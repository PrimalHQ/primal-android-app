package net.primal.android.core.utils

import android.util.Patterns

fun String.isValidUsername(): Boolean = all { it.isLetterOrDigit() }

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
