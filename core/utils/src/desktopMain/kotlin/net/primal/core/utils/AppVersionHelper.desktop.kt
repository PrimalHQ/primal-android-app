package net.primal.core.utils

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object AppBuildHelper {
    actual fun getAppVersion(): String {
        throw NotImplementedError()
    }

    actual fun getAppName(): String {
        throw NotImplementedError()
    }

    actual fun getPlatformName(): String {
        throw NotImplementedError()
    }
}
