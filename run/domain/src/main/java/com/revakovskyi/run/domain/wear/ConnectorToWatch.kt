package com.revakovskyi.run.domain.wear

import com.revakovskyi.core.connectivity.domain.DeviceNode
import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.core.connectivity.domain.messaging.MessagingError
import com.revakovskyi.core.domain.util.EmptyDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ConnectorToWatch {

    val connectedDevice: StateFlow<DeviceNode?>
    val messagingActions: Flow<MessagingAction>

    fun setIsTrackable(isTrackable: Boolean)
    suspend fun sendActionToWatch(action: MessagingAction): EmptyDataResult<MessagingError>

}
