package net.primal.core.utils

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppBuildHelper {
    fun getAppName() : String
    fun getAppVersion() : String
    fun getPlatformName() : String
}
