package net.primal.android.wallet.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import timber.log.Timber

@Singleton
class ExchangeRateHandler @Inject constructor(
    private val walletRepository: WalletRepository,
) {

    private val _state = MutableStateFlow(value = 0.00)
    val usdExchangeRate = _state.asStateFlow()
    private fun setState(reducer: Double.() -> Double) = _state.getAndUpdate { it.reducer() }

    suspend fun updateExchangeRate(userId: String) {
        try {
            val btcRate = walletRepository.getExchangeRate(userId = userId)
            setState { btcRate }
        } catch (error: SignatureException) {
            Timber.e(error)
        } catch (error: NetworkException) {
            Timber.e(error)
        }
    }
}

fun Double?.isValidExchangeRate() = this != null && this > 0
