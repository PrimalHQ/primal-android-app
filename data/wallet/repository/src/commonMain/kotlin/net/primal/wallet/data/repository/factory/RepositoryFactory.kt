package net.primal.wallet.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.data.remote.factory.NwcApiServiceFactory
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.TsunamiWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.billing.BillingRepository
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.rates.exchange.ExchangeRateRepository
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.tsunami.createTsunamiWalletSdk
import net.primal.wallet.data.lightning.LightningRepositoryImpl
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.factory.WalletApiServiceFactory
import net.primal.wallet.data.repository.BillingRepositoryImpl
import net.primal.wallet.data.repository.ExchangeRateRepositoryImpl
import net.primal.wallet.data.repository.PrimalWalletAccountRepositoryImpl
import net.primal.wallet.data.repository.TransactionFeeRepositoryImpl
import net.primal.wallet.data.repository.TsunamiWalletAccountRepositoryImpl
import net.primal.wallet.data.repository.WalletAccountRepositoryImpl
import net.primal.wallet.data.repository.WalletRepositoryImpl
import net.primal.wallet.data.service.factory.WalletServiceFactoryImpl

abstract class RepositoryFactory {

    private val dispatcherProvider = createDispatcherProvider()

    private val tsunamiWalletSdk = createTsunamiWalletSdk(
        dispatcher = dispatcherProvider.io(),
    )

    abstract fun resolveWalletDatabase(): WalletDatabase

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
                lightningRepository = LightningRepositoryImpl(
                    dispatcherProvider = dispatcherProvider,
                    lightningApi = NwcApiServiceFactory.createLightningApi(),
                ),
                eventRepository = eventRepository,
            ),
            tsunamiWalletService = WalletServiceFactoryImpl.createTsunamiWalletService(
                tsunamiWalletSdk = tsunamiWalletSdk,
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
    ): TransactionFeeRepository {
        return TransactionFeeRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
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

    fun createTsunamiWalletAccountRepository(): TsunamiWalletAccountRepository {
        return TsunamiWalletAccountRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            tsunamiWalletSdk = tsunamiWalletSdk,
            walletDatabase = resolveWalletDatabase(),
        )
    }
}
