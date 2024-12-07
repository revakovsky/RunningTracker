package com.revakovskyi.auth.presentation.signIn

sealed interface SignInAction {

    data object OnTogglePasswordVisibility : SignInAction
    data object OnSignInClick : SignInAction
    data object OnSignUpClick : SignInAction
    data class EmailEntered(val email: String) : SignInAction
    data class PasswordEntered(val password: String) : SignInAction

}
