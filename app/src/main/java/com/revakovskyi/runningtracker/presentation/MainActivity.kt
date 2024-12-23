package com.revakovskyi.runningtracker.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.revakovskyi.core.peresentation.ui.setUpImeWindowInsets
import com.revakovskyi.core.presentation.designsystem.theme.RunningTrackerTheme
import com.revakovskyi.runningtracker.R
import com.revakovskyi.runningtracker.nav.NavigationRoot
import com.revakovskyi.runningtracker.presentation.components.AnalyticsInstallDialog
import com.revakovskyi.runningtracker.utils.SplitInstallHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private lateinit var splitInstallManager: SplitInstallManager
    private var splitInstallHelper: SplitInstallHelper? = null

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.state.isCheckingAuthInfo }
        }
        setUpImeWindowInsets()

        initSplitInstallObjects()
        showAppContent()
    }

    /**
     * Initializes objects required for dynamic feature module management.
     * This sets up the `SplitInstallManager` and `SplitInstallHelper` to handle
     * module installation and interactions.
     */
    private fun initSplitInstallObjects() {
        splitInstallManager = SplitInstallManagerFactory.create(applicationContext)

        splitInstallHelper = SplitInstallHelper(
            splitInstallManager = splitInstallManager,
            onShowResultingMessage = { stringResId -> showToast(stringResId) },
            onChangeAnalyticsDialogVisibility = { isVisible -> viewModel.setAnalyticsDialogVisibility(isVisible) },
            onRequiresUserConfirmation = { state -> splitInstallManager.startConfirmationDialogForResult(state, this@MainActivity, 0) }
        )
    }

    /**
     * Displays the main content of the application.
     * Checks the authentication state to determine the appropriate navigation root.
     * Dynamically displays the analytics installation dialog if required.
     */
    private fun showAppContent() {
        setContent {
            RunningTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!viewModel.state.isCheckingAuthInfo) {
                        NavigationRoot(
                            isSignedIn = viewModel.state.isSignedIn,
                            onAnalyticsClick = { installOrStartAnalyticsFeature() }
                        )

                        if (viewModel.state.showAnalyticsInstallDialog) AnalyticsInstallDialog()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        splitInstallHelper?.registerListener()
    }

    override fun onPause() {
        super.onPause()
        splitInstallHelper?.unregisterListener()
    }

    /**
     * Handles the installation or starting of the analytics feature module.
     * If the module is already installed, it launches the corresponding activity;
     * otherwise, it initiates the installation process.
     */
    private fun installOrStartAnalyticsFeature() {
        val containsThisModule = splitInstallManager.installedModules.contains(ANALYTICS_MODULE_NAME)

        if (containsThisModule) launchAnalyticsActivity()
        else installAnalyticsActivity()
    }

    private fun launchAnalyticsActivity() {
        Intent()
            .setClassName(packageName, ANALYTICS_CLASS_NAME)
            .also(::startActivity)
    }

    /**
     * Initiates the installation of the Analytics feature module.
     * Displays a toast message if the installation fails.
     */
    private fun installAnalyticsActivity() {
        val request = SplitInstallRequest.newBuilder()
            .addModule(ANALYTICS_MODULE_NAME)
            .build()

        splitInstallManager
            .startInstall(request)
            .addOnFailureListener {
                it.printStackTrace()
                showToast(R.string.error_could_not_load_a_module)
            }
    }

    private fun showToast(@StringRes stringResId: Int) {
        Toast.makeText(
            applicationContext,
            getString(stringResId),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        splitInstallHelper = null
    }


    companion object {
        private const val ANALYTICS_MODULE_NAME = "analytics_feature"
        private const val ANALYTICS_CLASS_NAME = "com.revakovskyi.analytics.analytics_feature.AnalyticsActivity"
    }

}
