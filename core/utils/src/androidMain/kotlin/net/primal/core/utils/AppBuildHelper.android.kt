package net.primal.core.utils

class AndroidAppBuildHelper : AppBuildHelper {

    override fun getAppVersion(): String = AndroidBuildConfig.APP_VERSION

    override fun getAppName(): String = "Primal-Android"

    override fun getPlatformName(): String = "Android"
}

actual fun createAppBuildHelper(): AppBuildHelper = AndroidAppBuildHelper()
