package net.primal.android.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun copyText(
    text: String,
    context: Context,
    label: String = "",
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
