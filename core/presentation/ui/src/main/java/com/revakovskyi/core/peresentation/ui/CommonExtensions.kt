package com.revakovskyi.core.peresentation.ui

import android.content.Context
import android.widget.Toast

fun showToastError(error: String, context: Context) {
    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
}
