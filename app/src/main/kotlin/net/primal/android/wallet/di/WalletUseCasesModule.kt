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
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.builder.TxRequestBuilder
import net.primal.domain.usecase.ConnectNwcUseCase
import net.primal.domain.usecase.EnsurePrimalWalletExistsUseCase
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase
import net.primal.domain.usecase.RestoreSparkWalletUseCase
import net.primal.domain.wallet.SeedPhraseGenerator
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.builder.factory.TxRequestBuilderFactory
import net.primal.wallet.data.generator.RecoveryPhraseGenerator
import net.primal.wallet.data.repository.factory.WalletRepositoryFactory
import net.primal.wallet.data.repository.handler.MigratePrimalToSparkWalletHandler
import net.primal.wallet.data.repository.handler.MigratePrimalTransactionsHandler

@Module
@InstallIn(SingletonComponent::class)
object WalletUseCasesModule {

    @Provides
    @Singleton
    fun providesTxRequestBuilder(): TxRequestBuilder = TxRequestBuilderFactory.createTxRequestBuilder()

    @Provides
    @Singleton
    fun providesSeedPhraseGenerator(): SeedPhraseGenerator = RecoveryPhraseGenerator()

    @Provides
    @Singleton
    fun providesEnsurePrimalWalletExistsUseCase(
        primalWalletAccountRepository: PrimalWalletAccountRepository,
        walletAccountRepository: WalletAccountRepository,
    ): EnsurePrimalWalletExistsUseCase =
        EnsurePrimalWalletExistsUseCase(
            primalWalletAccountRepository = primalWalletAccountRepository,
            walletAccountRepository = walletAccountRepository,
        )

    @Provides
    @Singleton
    fun providesConnectNwcUseCase(
        walletRepository: WalletRepository,
        walletAccountRepository: WalletAccountRepository,
    ): ConnectNwcUseCase =
        ConnectNwcUseCase(
            walletRepository = walletRepository,
            walletAccountRepository = walletAccountRepository,
        )

    @Provides
    @Singleton
    fun providesEnsureSparkWalletExistsUseCase(
        sparkWalletManager: SparkWalletManager,
        sparkWalletAccountRepository: SparkWalletAccountRepository,
        walletAccountRepository: WalletAccountRepository,
        seedPhraseGenerator: SeedPhraseGenerator,
    ): EnsureSparkWalletExistsUseCase =
        EnsureSparkWalletExistsUseCase(
            sparkWalletManager = sparkWalletManager,
            sparkWalletAccountRepository = sparkWalletAccountRepository,
            walletAccountRepository = walletAccountRepository,
            seedPhraseGenerator = seedPhraseGenerator,
        )

    @Provides
    @Singleton
    fun providesRestoreSparkWalletUseCase(
        sparkWalletManager: SparkWalletManager,
        walletAccountRepository: WalletAccountRepository,
        sparkWalletAccountRepository: SparkWalletAccountRepository,
    ): RestoreSparkWalletUseCase =
        RestoreSparkWalletUseCase(
            sparkWalletManager = sparkWalletManager,
            walletAccountRepository = walletAccountRepository,
            sparkWalletAccountRepository = sparkWalletAccountRepository,
        )

    @Provides
    @Singleton
    fun providesMigratePrimalTransactionsHandler(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): MigratePrimalTransactionsHandler =
        WalletRepositoryFactory.createMigratePrimalTransactionsHandler(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
        )

    @Provides
    @Singleton
    fun providesMigratePrimalToSparkWalletHandler(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
        ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
        walletRepository: WalletRepository,
    ): MigratePrimalToSparkWalletHandler =
        WalletRepositoryFactory.createMigratePrimalToSparkWalletHandler(
            primalWalletApiClient = primalApiClient,
            nostrEventSignatureHandler = nostrNotary,
            ensureSparkWalletExistsUseCase = ensureSparkWalletExistsUseCase,
            walletRepository = walletRepository,
        )
}
