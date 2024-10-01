package com.revakovskyi.runningtracker.helpers

import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.revakovskyi.runningtracker.R

class SplitInstallHelper(
    private val splitInstallManager: SplitInstallManager,
    onChangeAnalyticsDialogVisibility: (isVisible: Boolean) -> Unit,
    onShowResultingMessage: (messageResId: Int) -> Unit,
    onRequiresUserConfirmation: (state: SplitInstallSessionState) -> Unit,
) {

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