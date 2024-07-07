package com.revakovskyi.runningtracker.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.revakovskyi.auth.presentation.intro.IntroScreenRoute
import com.revakovskyi.auth.presentation.signUp.SignUpScreenRoot

@Composable
fun NavigationRoot(
    navHostController: NavHostController,
) {

    NavHost(navController = navHostController, startDestination = "auth") {

        authGraph(navHostController)

    }

}

private fun NavGraphBuilder.authGraph(navHostController: NavHostController) {
    navigation(startDestination = "intro", route = "auth") {

        composable(route = "intro") {
            IntroScreenRoute(
                onSignInClick = { navHostController.navigate("sign_in") },
                onSignUpClick = { navHostController.navigate("sign_up") }
            )
        }

        composable(route = "sign_up") {
            SignUpScreenRoot(
                onSignInClick = {
                    navHostController.navigate("sign_in") {
                        popUpTo("sign_up") {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                onSuccessfulRegistration = { navHostController.navigate("sign_in") }
            )
        }

        composable(route = "sign_in") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Sign In")
            }
        }

    }
}