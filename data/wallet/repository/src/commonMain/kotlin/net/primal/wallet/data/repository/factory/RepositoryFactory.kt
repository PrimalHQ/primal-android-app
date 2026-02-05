package net.primal.wallet.data.repository.factory

import net.primal.core.lightning.LightningPayHelper
import net.primal.core.networking.nwc.wallet.NwcWalletRequestParser
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.billing.BillingRepository
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.nostr.NwcService
import net.primal.domain.connections.primal.PrimalWalletNwcRepository
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.rates.exchange.ExchangeRateRepository
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.nwc.NwcLogRepository
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.nwc.builder.NwcWalletResponseBuilder
import net.primal.wallet.data.nwc.manager.NwcBudgetManager
import net.primal.wallet.data.nwc.processor.NwcRequestProcessor
import net.primal.wallet.data.nwc.service.NwcServiceImpl
import net.primal.wallet.data.remote.factory.WalletApiServiceFactory
import net.primal.wallet.data.repository.BillingRepositoryImpl
import net.primal.wallet.data.repository.ExchangeRateRepositoryImpl
import net.primal.wallet.data.repository.InternalNwcLogRepository
import net.primal.wallet.data.repository.NwcLogRepositoryImpl
import net.primal.wallet.data.repository.NwcRepositoryImpl
import net.primal.wallet.data.repository.PrimalWalletAccountRepositoryImpl
import net.primal.wallet.data.repository.PrimalWalletNwcRepositoryImpl
import net.primal.wallet.data.repository.SparkWalletAccountRepositoryImpl
import net.primal.wallet.data.repository.SparkWalletManagerImpl
import net.primal.wallet.data.repository.TransactionFeeRepositoryImpl
import net.primal.wallet.data.repository.WalletAccountRepositoryImpl
import net.primal.wallet.data.repository.WalletRepositoryImpl
import net.primal.wallet.data.service.factory.WalletServiceFactoryImpl
import net.primal.wallet.data.spark.BreezApiKeyProvider
import net.primal.wallet.data.spark.BreezSdkInstanceManager
import net.primal.wallet.data.spark.BreezSdkStorageProvider

abstract class RepositoryFactory {

    private val dispatcherProvider = createDispatcherProvider()

    private val lightningPayHelper = LightningPayHelper(dispatcherProvider)

    private val breezSdkInstanceManager by lazy {
        BreezSdkInstanceManager(
            storageProvider = resolveBreezSdkStorageProvider(),
            apiKey = BreezApiKeyProvider.requireApiKey(),
        )
    }

    internal abstract fun resolveWalletDatabase(): WalletDatabase

    internal abstract fun resolveBreezSdkStorageProvider(): BreezSdkStorageProvider

    fun createWalletRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
        profileRepository: ProfileRepository,
        eventRepository: EventRepository,
    ): WalletRepository {
        return WalletRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletDatabase = resolveWalletDatabase(),
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
            profileRepository = profileRepository,
            walletServiceFactory = createWalletServiceFactory(
                primalWalletApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
                eventRepository = eventRepository,
            ),
        )
    }

    private fun createWalletServiceFactory(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
        eventRepository: EventRepository,
    ): WalletServiceFactoryImpl {
        return WalletServiceFactoryImpl(
            primalWalletService = WalletServiceFactoryImpl.createPrimalWalletService(
                primalWalletApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
            nostrWalletService = WalletServiceFactoryImpl.createNostrWalletService(
                eventRepository = eventRepository,
                lightningPayHelper = lightningPayHelper,
            ),
            sparkWalletService = WalletServiceFactoryImpl.createSparkWalletService(
                breezSdkInstanceManager = breezSdkInstanceManager,
                eventRepository = eventRepository,
            ),
        )
    }

    fun createBillingRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): BillingRepository {
        return BillingRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
        )
    }

    fun createTransactionFeeRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
        eventRepository: EventRepository,
    ): TransactionFeeRepository {
        return TransactionFeeRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletServiceFactory = createWalletServiceFactory(
                primalWalletApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
                eventRepository = eventRepository,
            ),
            walletDatabase = resolveWalletDatabase(),
        )
    }

    fun createExchangeRateRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): ExchangeRateRepository {
        return ExchangeRateRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
        )
    }

    fun createWalletAccountRepository(): WalletAccountRepository {
        return WalletAccountRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletDatabase = resolveWalletDatabase(),
        )
    }

    fun createPrimalWalletAccountRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): PrimalWalletAccountRepository {
        return PrimalWalletAccountRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletDatabase = resolveWalletDatabase(),
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
            signatureHandler = nostrEventSignatureHandler,
        )
    }

    fun createSparkWalletAccountRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): SparkWalletAccountRepository {
        return SparkWalletAccountRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
            walletDatabase = resolveWalletDatabase(),
        )
    }

    fun createSparkWalletManager(): SparkWalletManager {
        return SparkWalletManagerImpl(
            breezSdkInstanceManager = breezSdkInstanceManager,
        )
    }

    fun createPrimalWalletNwcRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): PrimalWalletNwcRepository {
        return PrimalWalletNwcRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            primalWalletNwcApi = WalletApiServiceFactory.createPrimalWalletNwcApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
        )
    }

    fun createNwcRepository(): NwcRepository =
        NwcRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = resolveWalletDatabase(),
        )

    fun createNwcLogRepository(): NwcLogRepository =
        NwcLogRepositoryImpl(
            walletDatabase = resolveWalletDatabase(),
            dispatchers = dispatcherProvider,
        )

    private fun createInternalNwcLogRepository(): InternalNwcLogRepository =
        InternalNwcLogRepository(
            walletDatabase = resolveWalletDatabase(),
            dispatchers = dispatcherProvider,
        )

    fun createNwcService(
        walletRepository: WalletRepository,
        nostrEncryptionService: NostrEncryptionService,
    ): NwcService {
        val responseBuilder = NwcWalletResponseBuilder()
        return NwcServiceImpl(
            dispatchers = dispatcherProvider,
            nwcRepository = createNwcRepository(),
            encryptionService = nostrEncryptionService,
            requestParser = NwcWalletRequestParser(
                encryptionService = nostrEncryptionService,
            ),
            requestProcessor = NwcRequestProcessor(
                walletRepository = walletRepository,
                nwcBudgetManager = NwcBudgetManager(
                    dispatcherProvider = dispatcherProvider,
                    walletDatabase = resolveWalletDatabase(),
                ),
                responseBuilder = responseBuilder,
                nwcLogRepository = createInternalNwcLogRepository(),
            ),
            responseBuilder = responseBuilder,
        )
    }
}
