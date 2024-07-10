@file:Suppress("OPT_IN_USAGE_FUTURE_ERROR")
@file:OptIn(ExperimentalFoundationApi::class)

package com.revakovskyi.auth.presentation.signIn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.revakovskyi.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class SignInViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    private val eventChannel = Channel<SignInEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: SignInAction) {
        when (action) {
            SignInAction.OnSignInClick -> signIn()
            SignInAction.OnSignUpClick -> Unit
            SignInAction.OnTogglePasswordVisibility -> state =
                state.copy(isPasswordVisible = !state.isPasswordVisible)
        }
    }

    private fun signIn() {
        // TODO
    }

}