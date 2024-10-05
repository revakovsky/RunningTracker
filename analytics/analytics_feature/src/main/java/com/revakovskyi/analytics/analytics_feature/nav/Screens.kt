package com.revakovskyi.analytics.analytics_feature.nav

sealed class Screens(val route: String) {

    data object Analytics : Screens(route = "analytics")

}
