package net.primal.core.init

import android.content.Context
import net.primal.PrimalLib
import org.koin.android.ext.koin.androidContext

fun initPrimal(context: Context) {
    PrimalLib.initKoin {
        androidContext(context)
    }
}
