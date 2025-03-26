package net.primal.android.signer.utils

import android.content.Context
import android.os.Build
import net.primal.android.signer.AMBER_PACKAGE_NAME

/* Amber 3.0.4 */
private const val COMPATIBLE_AMBER_VERSION_CODE = 115

fun isCompatibleAmberVersionInstalled(context: Context): Boolean = runCatching {
    val pm = context.packageManager
    val packageInfo = pm.getPackageInfo(AMBER_PACKAGE_NAME, 0)
    val installedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }

    installedVersionCode >= COMPATIBLE_AMBER_VERSION_CODE
}.getOrDefault(false)
