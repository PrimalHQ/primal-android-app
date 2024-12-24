package net.primal.android.wallet

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.wallet.repository.WalletRepository
import timber.log.Timber

@Singleton
class UsdExchangeRateHandler @Inject constructor(
    private val walletRepository: WalletRepository,
) {

    private val _state = MutableStateFlow(value = 0.00)
    val usdExchangeRate = _state.asStateFlow()
    private fun setState(reducer: Double.() -> Double) = _state.getAndUpdate { it.reducer() }

    suspend fun updateExchangeRate(userId: String) {
        try {
            val btcRate = walletRepository.getExchangeRate(userId = userId)
            setState { btcRate }
        } catch (error: WssException) {
            Timber.e(error)
        }
    }
}

fun Double?.isValidExchangeRate() = this != null && this > 0
