package com.revakovskyi.auth.domain

interface PatternValidator {

    fun matches(value: String): Boolean

}