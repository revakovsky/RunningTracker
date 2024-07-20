package com.revakovskyi.runningtracker.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.core.domain.auth.SessionStorage
import kotlinx.coroutines.launch

class MainViewModel(
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(isCheckingAuthInfo = true)
            state = state.copy(
                isSignedIn = sessionStorage.get() != null
            )
            state = state.copy(isCheckingAuthInfo = false)
        }
    }

}