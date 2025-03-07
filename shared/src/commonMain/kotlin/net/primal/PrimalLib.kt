package net.primal

import net.primal.core.coroutines.coroutinesModule
import net.primal.core.db.di.databaseModule
import net.primal.events.di.eventsModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

internal object PrimalLib {

    private var koinApp: KoinApplication? = null

    fun initKoin(config: KoinAppDeclaration? = null) {
        koinApp = startKoin {
            config?.invoke(this)
            modules(
                databaseModule(),
                coroutinesModule,
                eventsModule,
            )
        }
    }

    fun getKoin() = koinApp?.koin ?: error("Koin not initialized.")
}
