@file:OptIn(ExperimentalFoundationApi::class)

package com.revakovskyi.auth.presentation.signIn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState

data class SignInState(
    val email: TextFieldState = TextFieldState(),
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val canSignIn: Boolean = false,
    val isSigningIn: Boolean = false,
)
