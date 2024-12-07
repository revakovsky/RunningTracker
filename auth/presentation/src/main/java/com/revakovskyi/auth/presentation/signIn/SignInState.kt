package com.revakovskyi.auth.presentation.signIn

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val canSignIn: Boolean = false,
    val isSigningIn: Boolean = false,
)
