package com.revakovskyi.auth.data

import android.util.Patterns
import com.revakovskyi.auth.domain.PatternValidator

object EmailPatternValidator : PatternValidator {

    override fun matches(value: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(value).matches()
    }

}