package net.primal.core.init

import net.primal.PrimalLib

object PrimalInitializer {
    fun init(
        appName: String? = null,
        userAgent: String? = null,
    ) {
        PrimalLib.apply {
            initKoin()
            setAppName(appName)
            setUserAgent(userAgent)
        }
    }
}
