package com.revakovskyi.auth.presentation.signUp

sealed interface SignUpAction {

    data object OnTogglePasswordVisibilityClick : SignUpAction
    data object OnSignInClick : SignUpAction
    data object OnRegisterClick : SignUpAction

}
