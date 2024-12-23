package com.revakovskyi.auth.presentation.signUp

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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val userDataValidator: UserDataValidator,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(SignUpState())
        private set

    private val eventChannel = Channel<SignUpEvent>()
    val events = eventChannel.receiveAsFlow()


    /**
     * Updates the state to reflect the validity of user inputs and the ability to register.
     */
    init {
        snapshotFlow { state.email }
            .onEach { email ->
                val isValidEmail = userDataValidator.isValidEmail(email)

                state = state.copy(
                    isValidEmail = isValidEmail,
                    canRegister = isValidEmail && state.passwordValidationState.isValidPassword && !state.isRegistering
                )
            }
            .launchIn(viewModelScope)

        snapshotFlow { state.password }
            .onEach { password ->
                val passwordValidationState = userDataValidator.isValidPassword(password)

                state = state.copy(
                    passwordValidationState = passwordValidationState,
                    canRegister = state.isValidEmail && passwordValidationState.isValidPassword && !state.isRegistering
                )
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SignUpAction) {
        when (action) {
            SignUpAction.OnRegisterClick -> register()
            SignUpAction.OnSignInClick -> Unit
            SignUpAction.OnTogglePasswordVisibilityClick -> togglePasswordVisibility()
            is SignUpAction.EmailEntered -> emailEntered(action.email)
            is SignUpAction.PasswordEntered -> passwordEntered(action.password)
        }
    }

    private fun register() {
        viewModelScope.launch {
            state = state.copy(isRegistering = true)

            val result = authRepository.register(
                email = state.email.trim(),
                password = state.password
            )

            state = state.copy(isRegistering = false)

            when (result) {
                is Result.Error -> {
                    if (result.error == DataError.Network.CONFLICT) {
                        eventChannel.send(
                            SignUpEvent.Error(UiText.StringResource(R.string.error_email_exists))
                        )
                    } else eventChannel.send(SignUpEvent.Error(result.error.asUiText()))
                }

                is Result.Success -> eventChannel.send(SignUpEvent.RegistrationSuccess)
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