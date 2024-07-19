package com.revakovskyi.core.peresentation.ui

import androidx.core.app.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun ComponentActivity.setUpImeWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(android.R.id.content)
    ) { view, insets ->
        val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        view.updatePadding(bottom = bottom)
        insets
    }
}
