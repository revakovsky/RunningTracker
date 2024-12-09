package com.revakovskyi.runningtracker.nav

import kotlinx.serialization.Serializable

sealed interface Routes {

    @Serializable
    data object Auth : Routes {

        sealed interface Screens {

            @Serializable
            data object Intro : Screens

            @Serializable
            data object SignUp : Screens

            @Serializable
            data object SignIn : Screens

        }

    }

    @Serializable
    data object Run : Routes {

        sealed interface Screens {

            @Serializable
            data object RunOverView : Screens

            @Serializable
            data object ActiveRun : Screens

        }

    }

}
