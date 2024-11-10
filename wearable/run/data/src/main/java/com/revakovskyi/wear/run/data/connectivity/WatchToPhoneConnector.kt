package com.revakovskyi.wear.run.data.connectivity

import com.revakovskyi.core.connectivity.domain.DeviceNode
import com.revakovskyi.core.connectivity.domain.DeviceType
import com.revakovskyi.core.connectivity.domain.NodeDiscovery
import com.revakovskyi.wear.run.domain.phone.PhoneConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WatchToPhoneConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
) : PhoneConnector {

    private val _connectedDevice = MutableStateFlow<DeviceNode?>(null)
    override val connectedDevice = _connectedDevice.asStateFlow()

    val messagingActions = nodeDiscovery
        .observeConnectedDevices(localDeviceType = DeviceType.WATCH)
        .onEach { deviceNodes ->
            val node = deviceNodes.firstOrNull()

            if (node != null && node.isNearby) _connectedDevice.value = node
        }
        .launchIn(applicationScope)

}
