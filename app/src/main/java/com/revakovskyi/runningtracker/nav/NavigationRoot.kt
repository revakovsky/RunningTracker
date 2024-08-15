package com.revakovskyi.runningtracker.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.revakovskyi.auth.presentation.intro.IntroScreenRoute
import com.revakovskyi.auth.presentation.signIn.SignInScreenRoot
import com.revakovskyi.auth.presentation.signUp.SignUpScreenRoot
import com.revakovskyi.run.presentation.activeRun.ActiveRunScreenRoot
import com.revakovskyi.run.presentation.activeRun.service.ActiveRunService
import com.revakovskyi.run.presentation.runOverview.RunOverviewScreenRoot
import com.revakovskyi.runningtracker.presentation.MainActivity

@Composable
fun NavigationRoot(
    navHostController: NavHostController,
    isSignedIn: Boolean,
) {

    NavHost(
        navController = navHostController,
        startDestination = if (isSignedIn) "run" else "auth"
    ) {

        authGraph(navHostController)
        runGraph(navHostController)

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
            SignInScreenRoot(
                onSuccessfulSignIn = {
                    navHostController.navigate("run") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                },
                onSignUpClick = {
                    navHostController.navigate("sign_up") {
                        popUpTo("sign_in") {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                }
            )
        }

    }

}


private fun NavGraphBuilder.runGraph(navHostController: NavHostController) {
    navigation(startDestination = "run_overview", route = "run") {

        composable(route = "run_overview") {
            RunOverviewScreenRoot(
                onStartRunClick = {
                    navHostController.navigate("active_run")
                }
            )
        }

        composable(
            route = "active_run",
            deepLinks = listOf(
                navDeepLink { uriPattern = ActiveRunService.DEEP_LINK }
            )
        ) {
            val context = LocalContext.current

            ActiveRunScreenRoot(
                onServiceToggle = { shouldServiceRun ->
                    when (shouldServiceRun) {
                        true -> context.startService(
                            ActiveRunService.createServiceStartingIntent(context, MainActivity::class.java)
                        )

                        false -> context.startService(ActiveRunService.createServiceStoppingIntent(context))
                    }
                }
            )
        }

    }
}
