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

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneToWatchConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
    private val messagingClient: MessagingClient,
) : ConnectorToWatch {

    private val _connectedDevice = MutableStateFlow<DeviceNode?>(null)
    override val connectedDevice = _connectedDevice.asStateFlow()

    private val isTrackable = MutableStateFlow(false)

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

    override suspend fun sendActionToWatch(action: MessagingAction): EmptyDataResult<MessagingError> {
        return messagingClient.sendOrQueueAction(action)
    }

}
