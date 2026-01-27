package net.primal.android.wallet.repository

import io.github.aakira.napier.Napier
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.rates.exchange.ExchangeRateRepository

@Singleton
class ExchangeRateHandler @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
) {

    private val _state = MutableStateFlow(value = 0.00)
    val usdExchangeRate = _state.asStateFlow()
    private fun setState(reducer: Double.() -> Double) = _state.getAndUpdate { it.reducer() }

    suspend fun updateExchangeRate(userId: String) {
        try {
            val btcRate = exchangeRateRepository.getExchangeRate(userId = userId)
            setState { btcRate }
        } catch (error: SignatureException) {
            Napier.e(throwable = error) { "Failed to fetch exchange rate due to signature error." }
        } catch (error: NetworkException) {
            Napier.e(throwable = error) { "Failed to fetch exchange rate due to network error." }
        }
    }
}

fun Double?.isValidExchangeRate() = this != null && this > 0
