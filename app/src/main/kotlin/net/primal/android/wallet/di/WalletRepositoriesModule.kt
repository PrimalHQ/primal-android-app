package net.primal.android.wallet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.TsunamiWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.billing.BillingRepository
import net.primal.domain.connections.PrimalWalletNwcRepository
import net.primal.domain.events.EventRepository
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.rates.exchange.ExchangeRateRepository
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.repository.factory.WalletRepositoryFactory

@Module
@InstallIn(SingletonComponent::class)
object WalletRepositoriesModule {

    @Provides
    @Singleton
    fun providesBillingRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): BillingRepository =
        WalletRepositoryFactory.createBillingRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )

    @Provides
    @Singleton
    fun providesWalletRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
        profileRepository: ProfileRepository,
        eventRepository: EventRepository,
    ): WalletRepository =
        WalletRepositoryFactory.createWalletRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
            profileRepository = profileRepository,
            eventRepository = eventRepository,
        )

    @Provides
    @Singleton
    fun providesTransactionFeeRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): TransactionFeeRepository =
        WalletRepositoryFactory.createTransactionFeeRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )

    @Provides
    @Singleton
    fun providesExchangeRateRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): ExchangeRateRepository =
        WalletRepositoryFactory.createExchangeRateRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )

    @Provides
    @Singleton
    fun providesWalletAccountRepository(): WalletAccountRepository =
        WalletRepositoryFactory.createWalletAccountRepository()

    @Provides
    @Singleton
    fun providesPrimalWalletAccountRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): PrimalWalletAccountRepository =
        WalletRepositoryFactory.createPrimalWalletAccountRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )

    @Provides
    @Singleton
    fun providesTsunamiWalletAccountRepository(): TsunamiWalletAccountRepository =
        WalletRepositoryFactory.createTsunamiWalletAccountRepository()

    @Provides
    @Singleton
    fun providePrimalWalletNwcRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): PrimalWalletNwcRepository =
        WalletRepositoryFactory.createPrimalWalletNwcRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )
}
