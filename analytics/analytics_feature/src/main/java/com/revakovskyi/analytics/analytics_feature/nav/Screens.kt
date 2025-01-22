package com.revakovskyi.analytics.analytics_feature.nav

import kotlinx.serialization.Serializable

sealed interface Routes {

    @Serializable
    data object Analytics : Routes

}
