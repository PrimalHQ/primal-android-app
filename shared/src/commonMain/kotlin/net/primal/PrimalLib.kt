package net.primal

import net.primal.api.feeds.di.primalFeedApiModule
import net.primal.core.di.platformModule
import net.primal.events.di.eventsModule
import net.primal.networking.di.httpClientModule
import net.primal.networking.di.socketsModule
import net.primal.networking.primal.api.di.primalImportApiModule
import net.primal.networking.primal.upload.api.di.primalUploadApiModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

internal object PrimalLib {

    private var koinApp: KoinApplication? = null

    var userAgent: String? = null
        private set

    var appName: String? = null
        private set

    fun initKoin(config: KoinAppDeclaration? = null) {
        koinApp = startKoin {
            config?.invoke(this)
            modules(
                // Core
                platformModule(),
                httpClientModule,
                socketsModule,

                // Repositories
                eventsModule,

                // Apis
                primalImportApiModule,
                primalUploadApiModule,
                primalFeedApiModule,
            )
        }
    }

    fun setUserAgent(userAgent: String?) { this.userAgent = userAgent }

    /**
     * Sets the app name which is commonly used in
     * web socket subscription names as "$appName-UUID".
     */
    fun setAppName(appName: String?) { this.appName = appName }

    fun getKoin() = koinApp?.koin ?: error("Koin not initialized.")
}
