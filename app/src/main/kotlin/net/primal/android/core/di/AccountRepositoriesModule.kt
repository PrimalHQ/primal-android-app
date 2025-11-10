package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.data.account.repository.repository.factory.AccountRepositoryFactory
import net.primal.data.account.repository.repository.factory.RepositoryFactory
import net.primal.data.account.repository.service.factory.AccountServiceFactory
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionEventRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.NostrEncryptionService

@Module
@InstallIn(SingletonComponent::class)
object AccountRepositoriesModule {
    @Provides
    fun provideRepositoryFactory(): RepositoryFactory = AccountRepositoryFactory

    @Provides
    fun provideConnectionRepository(): ConnectionRepository = AccountRepositoryFactory.createConnectionRepository()

    @Provides
    fun provideSessionRepository(): SessionRepository = AccountRepositoryFactory.createSessionRepository()

    @Provides
    fun provideSessionEventRepository(): SessionEventRepository =
        AccountRepositoryFactory.createSessionEventRepository()

    @Provides
    fun provideNostrEncryptionService(): NostrEncryptionService = AccountServiceFactory.createNostrEncryptionService()
}
