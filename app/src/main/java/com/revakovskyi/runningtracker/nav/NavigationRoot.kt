package com.revakovskyi.runningtracker.nav

import android.content.Context
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
    onAnalyticsClick: () -> Unit,
) {

    NavHost(
        navController = navHostController,
        startDestination = if (isSignedIn) Routes.Run.route else Routes.Auth.route
    ) {

        authGraph(navHostController)
        runGraph(navHostController, onAnalyticsClick)

    }

}

private fun NavGraphBuilder.authGraph(navHostController: NavHostController) {
    navigation(startDestination = Screens.Intro.route, route = Routes.Auth.route) {

        composable(route = Screens.Intro.route) {
            IntroScreenRoute(
                onSignInClick = { navHostController.navigate(Screens.SignIn.route) },
                onSignUpClick = { navHostController.navigate(Screens.SignUp.route) }
            )
        }

        composable(route = Screens.SignUp.route) {
            SignUpScreenRoot(
                onSignInClick = {
                    navHostController.navigate(Screens.SignIn.route) {
                        popUpTo(Screens.SignUp.route) {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                onSuccessfulRegistration = { navHostController.navigate(Screens.SignIn.route) }
            )
        }

        composable(route = Screens.SignIn.route) {
            SignInScreenRoot(
                onSuccessfulSignIn = {
                    navHostController.navigate(Routes.Run.route) {
                        popUpTo(Routes.Auth.route) {
                            inclusive = true
                        }
                    }
                },
                onSignUpClick = {
                    navHostController.navigate(Screens.SignUp.route) {
                        popUpTo(Screens.SignIn.route) {
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


private fun NavGraphBuilder.runGraph(
    navHostController: NavHostController,
    onAnalyticsClick: () -> Unit,
) {
    navigation(startDestination = Screens.RunOverView.route, route = Routes.Run.route) {

        composable(route = Screens.RunOverView.route) {
            RunOverviewScreenRoot(
                onLogOutClick = {
                    navHostController.navigate(Routes.Auth.route) {
                        popUpTo(Routes.Run.route) {
                            inclusive = true
                        }
                    }
                },
                onAnalyticsClick = onAnalyticsClick,
                onStartRunClick = {
                    navHostController.navigate(Screens.ActiveRun.route)
                }
            )
        }

        composable(
            route = Screens.ActiveRun.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = ActiveRunService.DEEP_LINK }
            )
        ) {
            val context = LocalContext.current

            ActiveRunScreenRoot(
                onFinishRun = { navHostController.navigateUp() },
                onServiceToggle = { shouldServiceRun ->
                    when (shouldServiceRun) {
                        true -> startRunningService(context)
                        false -> stopRunningService(context)
                    }
                }
            )
        }

    }
}

private fun startRunningService(context: Context) {
    context.startService(
        ActiveRunService.createServiceStartingIntent(context, MainActivity::class.java)
    )
}

private fun stopRunningService(context: Context) {
    context.startService(
        ActiveRunService.createServiceStoppingIntent(context)
    )
}
