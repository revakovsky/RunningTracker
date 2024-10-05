package com.revakovskyi.runningtracker.nav

sealed class Screens(val route: String) {

    data object Intro : Screens(route = "intro")
    data object SignUp : Screens(route = "sign_up")
    data object SignIn : Screens(route = "sign_in")

    data object RunOverView : Screens(route = "run_overview")
    data object ActiveRun : Screens(route = "active_run")

}
