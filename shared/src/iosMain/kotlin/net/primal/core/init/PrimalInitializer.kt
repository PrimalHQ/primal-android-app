package net.primal.core.init

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
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

        Napier.base(DebugAntilog())
    }
}
