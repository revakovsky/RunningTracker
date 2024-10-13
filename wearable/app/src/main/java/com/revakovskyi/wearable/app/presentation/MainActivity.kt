package com.revakovskyi.wearable.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.revakovskyi.core.presentation.design_system_wear.RunningTrackerWearTheme
import com.revakovskyi.wear.run.presentation.TrackerScreenRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            RunningTrackerWearTheme {
                TrackerScreenRoot()
            }
        }
    }

}
