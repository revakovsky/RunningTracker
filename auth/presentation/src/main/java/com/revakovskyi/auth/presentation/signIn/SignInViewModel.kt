package com.revakovskyi.auth.presentation.signIn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.auth.domain.AuthRepository
import com.revakovskyi.auth.domain.UserDataValidator
import com.revakovskyi.auth.presentation.R
import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.core.peresentation.ui.UiText
import com.revakovskyi.core.peresentation.ui.asUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authRepository: AuthRepository,
    private val userDataValidator: UserDataValidator,
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    private val eventChannel = Channel<SignInEvent>()
    val events = eventChannel.receiveAsFlow()


    init {
        combine(
            snapshotFlow { state.email },
            snapshotFlow { state.password }
        ) { email, password ->
            state = state.copy(
                canSignIn = userDataValidator.isValidEmail(email.trim()) && password.isNotEmpty()
            )
        }.launchIn(viewModelScope)
    }

    fun onAction(action: SignInAction) {
        when (action) {
            SignInAction.OnSignInClick -> signIn()
            SignInAction.OnSignUpClick -> Unit
            SignInAction.OnTogglePasswordVisibility -> togglePasswordVisibility()
            is SignInAction.EmailEntered -> emailEntered(action.email)
            is SignInAction.PasswordEntered -> passwordEntered(action.password)
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            state = state.copy(isSigningIn = true)
            val result = authRepository.signIn(
                email = state.email.trim(),
                password = state.password
            )
            state = state.copy(isSigningIn = false)

            when (result) {
                is Result.Error -> {
                    if (result.error == DataError.Network.UNAUTHORIZED) {
                        eventChannel.send(
                            SignInEvent.Error(
                                UiText.StringResource(R.string.error_email_password_incorrect)
                            )
                        )
                    } else {
                        eventChannel.send(
                            SignInEvent.Error(result.error.asUiText())
                        )
                    }
                }

                is Result.Success -> eventChannel.send(SignInEvent.SignInSuccess)
            }
        }
    }

    private fun togglePasswordVisibility() {
        state = state.copy(isPasswordVisible = !state.isPasswordVisible)
    }

    private fun emailEntered(email: String) {
        state = state.copy(email = email)
    }

    private fun passwordEntered(password: String) {
        state = state.copy(password = password)
    }

}