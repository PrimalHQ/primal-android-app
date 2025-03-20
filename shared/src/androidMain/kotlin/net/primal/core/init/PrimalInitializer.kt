package net.primal.core.init

import android.content.Context
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import net.primal.PrimalLib
import org.koin.android.ext.koin.androidContext

object PrimalInitializer {
    fun init(context: Context, showLog: Boolean = false) {
        PrimalLib.apply {
            initKoin { androidContext(context) }
        }

        if (showLog) {
            Napier.base(DebugAntilog())
        }
    }
}
