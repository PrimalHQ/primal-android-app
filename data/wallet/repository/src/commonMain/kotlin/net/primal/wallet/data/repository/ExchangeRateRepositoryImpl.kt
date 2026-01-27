package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.rates.exchange.ExchangeRateRepository
import net.primal.wallet.data.remote.api.PrimalWalletApi

internal class ExchangeRateRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletApi: PrimalWalletApi,
) : ExchangeRateRepository {

    override suspend fun getExchangeRate(userId: String): Double {
        return withContext(dispatcherProvider.io()) {
            primalWalletApi.getExchangeRate(userId)
        }
    }
}
