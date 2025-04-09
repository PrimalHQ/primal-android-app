package net.primal.android.user.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.user.zaps.NostrZapperFactoryImpl
import net.primal.domain.nostr.zaps.NostrZapperFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class ZapModule {

    @Binds
    abstract fun bindNostrZapperFactory(impl: NostrZapperFactoryImpl): NostrZapperFactory
}
