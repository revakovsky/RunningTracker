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

class AnalyticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadKoinModules(
            listOf(analyticsPresentationModule, analyticsDataModule)
        )

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