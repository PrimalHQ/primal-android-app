package net.primal.android.core.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

fun Context.findComponentActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }

    return null
}
