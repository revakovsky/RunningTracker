package com.revakovskyi.run.domain

import com.revakovskyi.core.domain.location.LocationWithAltitude
import kotlinx.coroutines.flow.Flow

interface LocationObserver {

    fun observeLocation(interval: Long): Flow<LocationWithAltitude>

}
