package com.revakovskyi.run.data.connectivity

import com.revakovskyi.core.connectivity.domain.DeviceNode
import com.revakovskyi.core.connectivity.domain.DeviceType
import com.revakovskyi.core.connectivity.domain.NodeDiscovery
import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.core.connectivity.domain.messaging.MessagingClient
import com.revakovskyi.core.connectivity.domain.messaging.MessagingError
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.run.domain.wear.ConnectorToWatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

/**
 * A connector that facilitates communication between a phone and a connected smartwatch.
 * It establishes connections, listens for messages, and sends actions based on app logic.
 *
 * @param nodeDiscovery Used to discover connected wearable nodes (e.g., smartwatch devices).
 * @param applicationScope The scope in which this connector operates, ensuring lifecycle-aware coroutines.
 * @param messagingClient Manages sending and receiving messages between the phone and the smartwatch.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PhoneToWatchConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
    private val messagingClient: MessagingClient,
) : ConnectorToWatch {

    /**
     * Tracks the currently connected device node.
     * Updated when a suitable nearby node is discovered.
     */
    private val _connectedDevice = MutableStateFlow<DeviceNode?>(null)
    override val connectedDevice = _connectedDevice.asStateFlow()

    /**
     * Tracks whether the device is in a state where it can be monitored or tracked.
     */
    private val isTrackable = MutableStateFlow(false)

    /**
     * A flow of messaging actions received from the connected smartwatch.
     * It listens for connection requests and responds with trackable/untrackable status.
     *
     * - Discovers connected nodes using `NodeDiscovery`.
     * - Connects to a node if one is found and nearby.
     * - Processes incoming messaging actions such as connection requests.
     */
    override val messagingActions: Flow<MessagingAction> = nodeDiscovery
        .observeConnectedDevices(localDeviceType = DeviceType.PHONE)
        .flatMapLatest { deviceNodes ->
            val node = deviceNodes.firstOrNull()

            if (node != null && node.isNearby) {
                _connectedDevice.value = node
                messagingClient.connectToNode(node.id)
            } else flowOf()
        }
        .onEach { messagingAction ->
            if (messagingAction == MessagingAction.ConnectionRequest) {
                sendActionToWatch(
                    if (isTrackable.value) MessagingAction.Trackable
                    else MessagingAction.Untrackable
                )
            }
        }
        .shareIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly
        )


    /***
     * Monitors trackable state and sends connection status updates to the smartwatch.
     */
    init {
        _connectedDevice
            .filterNotNull()
            .flatMapLatest { isTrackable }
            .onEach { isTrackable ->
                sendActionToWatch(MessagingAction.ConnectionRequest)

                val action =
                    if (isTrackable) MessagingAction.Trackable
                    else MessagingAction.Untrackable
                sendActionToWatch(action)
            }
            .launchIn(applicationScope)
    }


    override fun setIsTrackable(isTrackable: Boolean) {
        this.isTrackable.value = isTrackable
    }

    /**
     * Sends a specific action to the smartwatch.
     * If no connection is currently available, the action is queued.
     *
     * @param action The `MessagingAction` to be sent to the smartwatch.
     * @return A `Result` indicating success or failure of the action transmission.
     */
    override suspend fun sendActionToWatch(action: MessagingAction): EmptyDataResult<MessagingError> {
        return messagingClient.sendOrQueueAction(action)
    }

}
