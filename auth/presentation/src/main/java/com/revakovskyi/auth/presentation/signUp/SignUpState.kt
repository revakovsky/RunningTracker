@file:OptIn(ExperimentalFoundationApi::class)

package com.revakovskyi.auth.presentation.signUp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.revakovskyi.auth.domain.PasswordValidationState

data class SignUpState(
    val email: TextFieldState = TextFieldState(),
    val isValidEmail: Boolean = false,
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val passwordValidationState: PasswordValidationState = PasswordValidationState(),
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false,
)
