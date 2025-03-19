package net.primal

import net.primal.data.remote.di.remoteApiModule
import net.primal.data.repository.di.repositoryModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

internal object PrimalLib {

    private var koinApp: KoinApplication? = null

    fun initKoin(config: KoinAppDeclaration? = null) {
        koinApp = startKoin {
            config?.invoke(this)
            modules(
                // Core
                platformModule(),

                // Repositories
                repositoryModule,

                // Apis
                remoteApiModule,
            )
        }
    }

    fun getKoin() = koinApp?.koin ?: error("Koin not initialized.")
}
