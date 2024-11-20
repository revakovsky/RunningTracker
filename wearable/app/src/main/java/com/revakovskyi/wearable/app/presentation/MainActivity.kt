package com.revakovskyi.wearable.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.revakovskyi.core.notification.ActiveRunService
import com.revakovskyi.core.presentation.design_system_wear.RunningTrackerWearTheme
import com.revakovskyi.wear.run.presentation.TrackerScreenRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            RunningTrackerWearTheme {
                TrackerScreenRoot(
                    onServiceToggle = { shouldStartRunning ->
                        if (shouldStartRunning) startRunningService()
                        else stopRunningService()
                    }
                )
            }
        }
    }

    private fun startRunningService() {
        startService(
            ActiveRunService.createServiceStartingIntent(applicationContext, this::class.java)
        )
    }

    private fun stopRunningService() {
        startService(
            ActiveRunService.createServiceStoppingIntent(applicationContext)
        )
    }

}
