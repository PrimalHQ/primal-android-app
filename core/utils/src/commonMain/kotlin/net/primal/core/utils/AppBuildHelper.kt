package net.primal.core.utils

interface AppBuildHelper {
    fun getAppName(): String
    fun getAppVersion(): String
    fun getPlatformName(): String
}

expect fun createAppBuildHelper(): AppBuildHelper
