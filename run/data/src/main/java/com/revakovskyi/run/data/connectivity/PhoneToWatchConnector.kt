package com.revakovskyi.run.data.connectivity

import com.revakovskyi.core.connectivity.domain.DeviceNode
import com.revakovskyi.core.connectivity.domain.DeviceType
import com.revakovskyi.core.connectivity.domain.NodeDiscovery
import com.revakovskyi.run.domain.wear.WatchConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PhoneToWatchConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
) : WatchConnector {

    private val _connectedDevice = MutableStateFlow<DeviceNode?>(null)
    override val connectedDevice = _connectedDevice.asStateFlow()

    private val isTrackable = MutableStateFlow(false)

    val messagingActions = nodeDiscovery
        .observeConnectedDevices(localDeviceType = DeviceType.PHONE)
        .onEach { deviceNodes ->
            val node = deviceNodes.firstOrNull()

            if (node != null && node.isNearby) _connectedDevice.value = node
        }
        .launchIn(applicationScope)


    override fun setIsTrackable(isTrackable: Boolean) {
        this.isTrackable.value = isTrackable
    }

}
