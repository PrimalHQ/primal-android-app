package net.primal.android.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.billing.BillingRepository
import net.primal.domain.builder.TxRequestBuilder
import net.primal.domain.nostr.lightning.LightningRepository
import net.primal.domain.parser.WalletTextParser
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.rates.exchange.ExchangeRateRepository
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.builder.factory.TxRequestBuilderFactory
import net.primal.wallet.data.parser.factory.ParserFactory
import net.primal.wallet.data.repository.factory.WalletRepositoryFactory

@Module
@InstallIn(SingletonComponent::class)
object WalletRepositoriesModule {

    @Provides
    fun providesBillingRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): BillingRepository {
        return WalletRepositoryFactory.createBillingRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )
    }

    @Provides
    fun providesWalletRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
        profileRepository: ProfileRepository,
        lightningRepository: LightningRepository,
    ): WalletRepository {
        return WalletRepositoryFactory.createWalletRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
            profileRepository = profileRepository,
            lightningRepository = lightningRepository,
        )
    }

    @Provides
    fun providesTransactionFeeRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): TransactionFeeRepository {
        return WalletRepositoryFactory.createTransactionFeeRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )
    }

    @Provides
    fun providesExchangeRateRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): ExchangeRateRepository {
        return WalletRepositoryFactory.createExchangeRateRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )
    }

    @Provides
    fun providesWalletAccountRepository(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): WalletAccountRepository {
        return WalletRepositoryFactory.createWalletAccountRepository(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )
    }

    @Provides
    fun providesWalletTextParser(walletRepository: WalletRepository): WalletTextParser {
        return ParserFactory.createWalletTextParser(walletRepository = walletRepository)
    }

    @Provides
    fun providesTxRequestBuilder(): TxRequestBuilder {
        return TxRequestBuilderFactory.createTxRequestBuilder()
    }
}
