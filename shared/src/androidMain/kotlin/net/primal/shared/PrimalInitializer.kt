package net.primal.shared

import android.content.Context

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object PrimalInitializer {

    internal var appContext: Context? = null
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
