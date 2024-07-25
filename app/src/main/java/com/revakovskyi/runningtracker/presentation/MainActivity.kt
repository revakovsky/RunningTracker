package com.revakovskyi.runningtracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.revakovskyi.core.peresentation.ui.setUpImeWindowInsets
import com.revakovskyi.core.presentation.designsystem.theme.RunningTrackerTheme
import com.revakovskyi.runningtracker.nav.NavigationRoot
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.state.isCheckingAuthInfo }
        }
        setUpImeWindowInsets()
        setContent {
            RunningTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navHostController = rememberNavController()
                    if (!viewModel.state.isCheckingAuthInfo) {
                        NavigationRoot(
                            navHostController = navHostController,
                            isSignedIn = viewModel.state.isSignedIn
                        )
                    }
                }
            }
        }
    }

}
