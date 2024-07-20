package com.revakovskyi.runningtracker.presentation

data class MainState(
    val isSignedIn: Boolean = false,
    val isCheckingAuthInfo: Boolean = false,
)
