package net.primal.core.init

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import net.primal.PrimalLib

object PrimalInitializer {
    fun init(showLog: Boolean = false) {
        PrimalLib.apply {
            initKoin()
        }

        if (showLog) {
            Napier.base(DebugAntilog())
        }
    }
}
