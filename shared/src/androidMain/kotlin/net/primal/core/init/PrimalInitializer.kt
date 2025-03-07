package net.primal.core.init

import android.content.Context
import net.primal.PrimalLib
import org.koin.android.ext.koin.androidContext

object PrimalInitializer {
    fun init(
        context: Context,
        appName: String? = null,
        userAgent: String? = null,
    ) {
        PrimalLib.apply {
            initKoin { androidContext(context) }
            setAppName(appName)
            setUserAgent(userAgent)
        }
    }
}
