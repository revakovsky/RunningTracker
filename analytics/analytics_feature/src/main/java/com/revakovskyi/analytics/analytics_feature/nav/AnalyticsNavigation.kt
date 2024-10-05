package com.revakovskyi.analytics.analytics_feature.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.revakovskyi.analytics.presentation.AnalyticsScreenRoot

@Composable
fun AnalyticsNavigation(
    onCloseAnalytics: () -> Unit,
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.Analytics.route
    ) {

        composable(route = Screens.Analytics.route) {
            AnalyticsScreenRoot(onBackClick = onCloseAnalytics)
        }

    }

}