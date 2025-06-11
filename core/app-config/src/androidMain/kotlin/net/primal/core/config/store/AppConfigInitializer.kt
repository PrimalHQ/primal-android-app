package net.primal.core.config.store

import android.content.Context

typealias AppConfigInitializer = AndroidAppConfigInitializer

object AndroidAppConfigInitializer {

    internal var appContext: Context? = null
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
