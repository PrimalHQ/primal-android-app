package net.primal.wallet.data.spark

import kotlin.concurrent.Volatile
import net.primal.wallet.data.spark.BreezApiKeyProvider.init

/**
 * Provides the Breez API key for Spark wallet SDK.
 *
 * Must be initialized before using Spark wallet features.
 * - Android: Call [init] in Application.onCreate()
 * - iOS: Call init() before using WalletRepositoryFactory
 *
 * Thread-safe: Uses @Volatile for visibility across threads.
 * Note: Init is expected to be called only once from Application.onCreate on the main thread.
 */
internal object BreezApiKeyProvider {

    @Volatile
    private var apiKey: String? = null

    /**
     * Initializes the Breez API key.
     * Should be called only once from Application.onCreate on the main thread.
     *
     * @param apiKey The Breez API key obtained from Breez
     * @throws IllegalStateException if already initialized with a different key
     */
    fun init(apiKey: String) {
        val current = this.apiKey
        if (current != null && current != apiKey) {
            error("BreezApiKeyProvider already initialized with a different key")
        }
        this.apiKey = apiKey
    }

    /**
     * Returns the configured API key.
     *
     * @throws IllegalStateException if not initialized
     */
    fun requireApiKey(): String {
        return apiKey ?: error("BreezApiKeyProvider not initialized. Call init() first.")
    }
}
