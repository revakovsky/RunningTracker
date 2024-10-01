package com.revakovskyi.analytics.analytics_feature.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.revakovskyi.analytics.presentation.AnalyticsScreenRoot

private const val ANALYTICS_ROUT = "analytics_dashboard"

@Composable
fun AnalyticsNavigation(
    onCloseAnalytics: () -> Unit,
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ANALYTICS_ROUT
    ) {

        composable(ANALYTICS_ROUT) {
            AnalyticsScreenRoot(onBackClick = onCloseAnalytics)
        }

    }

}