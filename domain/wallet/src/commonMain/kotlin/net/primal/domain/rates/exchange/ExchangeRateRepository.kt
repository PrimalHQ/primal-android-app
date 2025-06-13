package net.primal.domain.rates.exchange

import kotlin.coroutines.cancellation.CancellationException
import net.primal.domain.common.exception.NetworkException

interface ExchangeRateRepository {

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun getExchangeRate(userId: String): Double
}
