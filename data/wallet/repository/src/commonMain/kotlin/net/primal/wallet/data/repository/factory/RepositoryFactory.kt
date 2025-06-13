package net.primal.wallet.data.repository.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.billing.BillingRepository
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.rates.exchange.ExchangeRateRepository
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.factory.WalletApiServiceFactory
import net.primal.wallet.data.repository.BillingRepositoryImpl
import net.primal.wallet.data.repository.ExchangeRateRepositoryImpl
import net.primal.wallet.data.repository.TransactionFeeRepositoryImpl
import net.primal.wallet.data.repository.WalletAccountRepositoryImpl
import net.primal.wallet.data.repository.WalletRepositoryImpl

abstract class RepositoryFactory {

    private val dispatcherProvider = createDispatcherProvider()

    abstract fun resolveWalletDatabase(): WalletDatabase

    fun createWalletRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
        profileRepository: ProfileRepository,
    ): WalletRepository {
        return WalletRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletDatabase = resolveWalletDatabase(),
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
            profileRepository = profileRepository,
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

    fun createWalletAccountRepository(
        primalWalletApiClient: PrimalApiClient,
        nostrEventSignatureHandler: NostrEventSignatureHandler,
    ): WalletAccountRepository {
        return WalletAccountRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            walletDatabase = resolveWalletDatabase(),
            primalWalletApi = WalletApiServiceFactory.createPrimalWalletApi(
                primalApiClient = primalWalletApiClient,
                nostrEventSignatureHandler = nostrEventSignatureHandler,
            ),
            signatureHandler = nostrEventSignatureHandler,
        )
    }
}
