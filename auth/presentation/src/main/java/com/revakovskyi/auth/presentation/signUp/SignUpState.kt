package com.revakovskyi.auth.presentation.signUp

import com.revakovskyi.auth.domain.PasswordValidationState

data class SignUpState(
    val isValidEmail: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordValidationState: PasswordValidationState = PasswordValidationState(),
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false,
)
