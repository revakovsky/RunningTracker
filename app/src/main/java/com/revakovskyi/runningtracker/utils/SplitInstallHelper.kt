package com.revakovskyi.runningtracker.utils

import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.revakovskyi.runningtracker.R

/**
 * A helper class to manage dynamic feature installation using SplitInstallManager.
 *
 * @property splitInstallManager The manager responsible for handling dynamic feature module installation.
 * @param onChangeAnalyticsDialogVisibility Callback to handle visibility of the analytics installation dialog.
 * @param onShowResultingMessage Callback to show messages based on installation status.
 * @param onRequiresUserConfirmation Callback invoked when user confirmation is required to proceed with installation.
 */
class SplitInstallHelper(
    private val splitInstallManager: SplitInstallManager,
    onChangeAnalyticsDialogVisibility: (isVisible: Boolean) -> Unit,
    onShowResultingMessage: (messageResId: Int) -> Unit,
    onRequiresUserConfirmation: (state: SplitInstallSessionState) -> Unit,
) {

    /**
     * Listener for updates to the split installation process.
     * Handles various statuses such as downloading, requiring user confirmation, installing,
     * installed, and failed.
     */
    private val splitInstallListener = SplitInstallStateUpdatedListener { state ->
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> onChangeAnalyticsDialogVisibility(true)
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> onRequiresUserConfirmation(state)
            SplitInstallSessionStatus.INSTALLING -> onChangeAnalyticsDialogVisibility(true)

            SplitInstallSessionStatus.INSTALLED -> {
                onChangeAnalyticsDialogVisibility(false)
                onShowResultingMessage(R.string.analytics_feature_was_successfully_installed)
            }

            SplitInstallSessionStatus.FAILED -> {
                onChangeAnalyticsDialogVisibility(false)
                onShowResultingMessage(R.string.unfortunately_the_installation_failed)
            }

            else -> Unit
        }
    }

    fun registerListener() {
        splitInstallManager.registerListener(splitInstallListener)
    }

    fun unregisterListener() {
        splitInstallManager.unregisterListener(splitInstallListener)
    }

}