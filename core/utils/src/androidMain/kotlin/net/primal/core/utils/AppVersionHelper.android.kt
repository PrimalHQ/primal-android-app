package net.primal.core.utils

import android.content.Context
import android.content.pm.PackageManager

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object AppBuildHelper {

    private const val UNKNOWN_VERSION = "unknown"

    actual fun getAppVersion(): String = AndroidBuildConfig.APP_VERSION

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: UNKNOWN_VERSION
        } catch (e: PackageManager.NameNotFoundException) {
            UNKNOWN_VERSION
        }
    }

    actual fun getAppName(): String = "Primal-Android"

    actual fun getPlatformName(): String = "Android"
}
