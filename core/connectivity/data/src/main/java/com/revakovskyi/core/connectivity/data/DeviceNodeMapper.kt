package com.revakovskyi.core.connectivity.data

import com.google.android.gms.wearable.Node
import com.revakovskyi.core.connectivity.domain.DeviceNode

fun Node.toDeviceNode(): DeviceNode {
    return DeviceNode(
        id = id,
        displayName = displayName,
        isNearby = isNearby,
    )
}