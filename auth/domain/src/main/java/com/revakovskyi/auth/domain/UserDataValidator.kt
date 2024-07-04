package com.revakovskyi.auth.domain

class UserDataValidator(
    private val validator: PatternValidator,
) {

    fun isValidEmail(email: String): Boolean = validator.matches(email.trim())

    fun isValidPassword(password: String): PasswordValidationState {
        return PasswordValidationState(
            hasMinLength = password.length >= MIN_PASSWORD_LENGTH,
            hasNumber = password.any { it.isDigit() },
            hasLowerCaseCharacter = password.any { it.isLowerCase() },
            hasUpperCaseCharacter = password.any { it.isUpperCase() },
        )
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 9
    }

}