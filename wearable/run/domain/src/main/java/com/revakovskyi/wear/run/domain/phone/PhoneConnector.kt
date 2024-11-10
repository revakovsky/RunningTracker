package com.revakovskyi.wear.run.domain.phone

import com.revakovskyi.core.connectivity.domain.DeviceNode
import kotlinx.coroutines.flow.StateFlow

interface PhoneConnector {

    val connectedDevice: StateFlow<DeviceNode?>

}
