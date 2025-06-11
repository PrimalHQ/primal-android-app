package net.primal.core.utils

class DesktopAppBuildHelper : AppBuildHelper {
    override fun getAppVersion(): String {
        throw NotImplementedError()
    }

    override fun getAppName(): String {
        throw NotImplementedError()
    }

    override fun getPlatformName(): String {
        throw NotImplementedError()
    }
}

actual fun createAppBuildHelper(): AppBuildHelper = DesktopAppBuildHelper()
