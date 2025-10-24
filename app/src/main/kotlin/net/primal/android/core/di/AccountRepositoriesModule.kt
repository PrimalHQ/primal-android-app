package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.data.account.repository.repository.factory.AccountRepositoryFactory
import net.primal.data.account.repository.service.factory.AccountServiceFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.service.NostrEncryptionService

@Module
@InstallIn(SingletonComponent::class)
object AccountRepositoriesModule {
    @Provides
    fun provideConnectionRepository(): ConnectionRepository = AccountRepositoryFactory.createConnectionRepository()

    @Provides
    fun provideNostrEncryptionService(): NostrEncryptionService = AccountServiceFactory.createNostrEncryptionService()
}
