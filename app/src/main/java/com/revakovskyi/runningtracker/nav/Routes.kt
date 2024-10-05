package com.revakovskyi.runningtracker.nav

sealed class Routes(val route: String) {

    data object Auth : Routes(route = "auth")
    data object Run : Routes(route = "run")

}
