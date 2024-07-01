package com.revakovskyi.auth.presentation.intro

sealed class IntroAction {
    data object OnSignInClick : IntroAction()
    data object OnSignUpClick : IntroAction()
}
