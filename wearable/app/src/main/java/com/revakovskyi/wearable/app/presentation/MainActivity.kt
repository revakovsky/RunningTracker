package com.revakovskyi.wearable.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.revakovskyi.core.presentation.design_system_wear.RunningTrackerWearTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            RunningTrackerWearTheme {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Hello, World!")
                    }

                }

            }
        }
    }

}
