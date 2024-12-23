package com.revakovskyi.analytics.analytics_feature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.android.play.core.splitcompat.SplitCompat
import com.revakovskyi.analytics.analytics_feature.nav.AnalyticsNavigation
import com.revakovskyi.analytics.data.di.analyticsDataModule
import com.revakovskyi.analytics.presentation.di.analyticsPresentationModule
import com.revakovskyi.core.presentation.designsystem.theme.RunningTrackerTheme
import org.koin.core.context.loadKoinModules

/**
 * Activity responsible for handling analytics-related functionality.
 *
 * This activity is part of a dynamically installed module. It initializes necessary dependencies,
 * integrates with the base application, and sets up the UI for analytics features.
 */
class AnalyticsActivity : ComponentActivity() {

    /**
     * Initializes the activity, loads required dependencies, and sets up the content.
     *
     * Steps:
     * 1. Dynamically loads Koin modules specific to the analytics feature.
     * 2. Ensures compatibility with split-installation using `SplitCompat`.
     * 3. Sets up the Compose-based UI using `AnalyticsNavigation`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadKoinModules(
            listOf(analyticsPresentationModule, analyticsDataModule)
        )

        /**
         * Ensures compatibility with split-installation using `SplitCompat`.
         *
         * `SplitCompat` is part of the Play Feature Delivery mechanism. It provides runtime compatibility
         * for dynamically delivered feature modules, ensuring that resources and code from those modules
         * are properly loaded and accessible within the app.
         */
        SplitCompat.installActivity(this@AnalyticsActivity)

        setContent {
            RunningTrackerTheme {
                AnalyticsNavigation(
                    onCloseAnalytics = { finish() }
                )
            }
        }
    }

}