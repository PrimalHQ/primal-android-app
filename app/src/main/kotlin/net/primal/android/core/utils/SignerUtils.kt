package net.primal.android.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/* https://github.com/greenart7c3/Amber/blob/master/docs/Android.md */
fun isExternalSignerInstalled(context: Context): Boolean {
    val intent =
        Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("nostrsigner:")
        }
    val infos = context.packageManager.queryIntentActivities(intent, 0)
    return infos.isNotEmpty()
}
