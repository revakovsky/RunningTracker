package com.revakovskyi.wear.run.data.connectivity

import com.revakovskyi.core.connectivity.domain.DeviceNode
import com.revakovskyi.core.connectivity.domain.DeviceType
import com.revakovskyi.core.connectivity.domain.NodeDiscovery
import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.core.connectivity.domain.messaging.MessagingClient
import com.revakovskyi.core.connectivity.domain.messaging.MessagingError
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.wear.run.domain.phone.ConnectorToPhone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn

/**
 * A connector class that facilitates communication between a smartwatch and a phone.
 *
 * @param nodeDiscovery An implementation of `NodeDiscovery` to observe connected devices.
 * @param applicationScope The coroutine scope used for managing asynchronous operations.
 * @param messagingClient An implementation of `MessagingClient` to handle message sending and receiving.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WatchToPhoneConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
    private val messagingClient: MessagingClient,
) : ConnectorToPhone {

    private val _connectedDevice = MutableStateFlow<DeviceNode?>(null)
    override val connectedDevice = _connectedDevice.asStateFlow()

    /**
     * A `Flow` of `MessagingAction` that listens to actions received from the phone.
     * It observes connected devices of type `WATCH` and connects to the first nearby node if available.
     *
     * The actions are shared across the application scope.
     */
    override val messagingActions: Flow<MessagingAction> = nodeDiscovery
        .observeConnectedDevices(localDeviceType = DeviceType.WATCH)
        .flatMapLatest { deviceNodes ->
            val node = deviceNodes.firstOrNull()

            if (node != null && node.isNearby) {
                _connectedDevice.value = node
                messagingClient.connectToNode(node.id)
            } else flowOf()
        }
        .shareIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly
        )

    /**
     * Sends a `MessagingAction` to the connected phone. If the connection is unavailable, the action
     * will be queued for later delivery.
     *
     * @param action The `MessagingAction` to send.
     * @return A result indicating success or a specific error.
     */
    override suspend fun sendActionToPhone(action: MessagingAction): EmptyDataResult<MessagingError> {
        return messagingClient.sendOrQueueAction(action)
    }

}
