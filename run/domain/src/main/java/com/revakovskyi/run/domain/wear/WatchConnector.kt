package com.revakovskyi.run.domain.wear

import com.revakovskyi.core.connectivity.domain.DeviceNode
import kotlinx.coroutines.flow.StateFlow

interface WatchConnector {

    val connectedDevice: StateFlow<DeviceNode?>

    fun setIsTrackable(isTrackable: Boolean)

}
