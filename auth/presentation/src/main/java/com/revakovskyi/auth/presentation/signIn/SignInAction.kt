package com.revakovskyi.auth.presentation.signIn

sealed interface SignInAction {

    data object OnTogglePasswordVisibility : SignInAction
    data object OnSignInClick : SignInAction
    data object OnSignUpClick : SignInAction

}
