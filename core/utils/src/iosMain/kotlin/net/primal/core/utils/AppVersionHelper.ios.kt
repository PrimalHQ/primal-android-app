package net.primal.core.utils

import platform.Foundation.NSBundle

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object AppBuildHelper {

    private const val UNKNOWN_VERSION = "unknown"

    actual fun getAppVersion(): String {
        return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: UNKNOWN_VERSION
    }

    actual fun getAppName(): String = "Primal-iOS"

    actual fun getPlatformName(): String = "iOS"
}
