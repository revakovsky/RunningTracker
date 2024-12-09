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
import com.revakovskyi.core.notification.ActiveRunService
import com.revakovskyi.run.presentation.activeRun.ActiveRunScreenRoot
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
        startDestination = if (isSignedIn) Routes.Run else Routes.Auth
    ) {
        authGraph(navHostController)
        runGraph(navHostController, onAnalyticsClick)
    }

}

private fun NavGraphBuilder.authGraph(navHostController: NavHostController) {

    navigation<Routes.Auth>(startDestination = Routes.Auth.Screens.Intro) {

        composable<Routes.Auth.Screens.Intro> {
            IntroScreenRoute(
                onSignInClick = { navHostController.navigate(Routes.Auth.Screens.SignIn) },
                onSignUpClick = { navHostController.navigate(Routes.Auth.Screens.SignUp) }
            )
        }

        composable<Routes.Auth.Screens.SignUp> {
            SignUpScreenRoot(
                onSignInClick = {
                    navHostController.navigate(Routes.Auth.Screens.SignIn) {
                        popUpTo(Routes.Auth.Screens.SignUp) {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                onSuccessfulRegistration = { navHostController.navigate(Routes.Auth.Screens.SignIn) }
            )
        }

        composable<Routes.Auth.Screens.SignIn> {
            SignInScreenRoot(
                onSuccessfulSignIn = {
                    navHostController.navigate(Routes.Run) {
                        popUpTo(Routes.Auth) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navHostController.navigate(Routes.Auth.Screens.SignUp) {
                        popUpTo(Routes.Auth.Screens.SignIn) {
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
    navigation<Routes.Run>(startDestination = Routes.Run.Screens.RunOverView) {

        composable<Routes.Run.Screens.RunOverView> {
            RunOverviewScreenRoot(
                onLogOutClick = {
                    navHostController.navigate(Routes.Auth) {
                        popUpTo(Routes.Run) { inclusive = true }
                    }
                },
                onAnalyticsClick = onAnalyticsClick,
                onStartRunClick = { navHostController.navigate(Routes.Run.Screens.ActiveRun) }
            )
        }

        composable<Routes.Run.Screens.ActiveRun>(
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
