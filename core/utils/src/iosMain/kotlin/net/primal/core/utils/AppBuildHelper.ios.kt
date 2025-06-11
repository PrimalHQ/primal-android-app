package net.primal.core.utils

import platform.Foundation.NSBundle

class IosAppBuildHelper : AppBuildHelper {

    private companion object {
        private const val UNKNOWN_VERSION = "unknown"
    }

    override fun getAppVersion(): String {
        return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString")
            as? String ?: UNKNOWN_VERSION
    }

    override fun getAppName(): String = "Primal-iOS"

    override fun getPlatformName(): String = "iOS"
}

actual fun createAppBuildHelper(): AppBuildHelper = IosAppBuildHelper()
