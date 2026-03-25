package net.primal.android.namecoin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.namecoin.NamecoinCertStore
import net.primal.android.namecoin.electrumx.ElectrumxClient
import net.primal.android.namecoin.electrumx.NamecoinNameResolver

@Module
@InstallIn(SingletonComponent::class)
object NamecoinModule {

    @Provides
    @Singleton
    fun provideElectrumxClient(
        certStore: NamecoinCertStore,
    ): ElectrumxClient {
        val client = ElectrumxClient()
        // Load user-pinned certs from disk on startup
        try {
            val pinnedCerts = certStore.loadPinnedCerts()
            if (pinnedCerts.isNotEmpty()) {
                client.setDynamicCerts(pinnedCerts)
            }
        } catch (_: Exception) {
            // Non-fatal — defaults will still work
        }
        return client
    }

    @Provides
    @Singleton
    fun provideNamecoinNameResolver(
        electrumxClient: ElectrumxClient,
    ): NamecoinNameResolver = NamecoinNameResolver(electrumxClient = electrumxClient)
}
