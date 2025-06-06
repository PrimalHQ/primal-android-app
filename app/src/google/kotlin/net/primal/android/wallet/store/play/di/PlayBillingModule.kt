package net.primal.android.wallet.store.play.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.play.PlayBillingClient
import net.primal.core.utils.coroutines.DispatcherProvider

@Module
@InstallIn(SingletonComponent::class)
object PlayBillingModule {

    @Provides
    @Singleton
    fun providePlayBillingClient(
        @ApplicationContext appContext: Context,
        dispatchers: DispatcherProvider,
    ): PrimalBillingClient =
        PlayBillingClient(
            appContext = appContext,
            dispatchers = dispatchers,
        )
}
