package net.primal.domain.wallet

import net.primal.domain.common.exception.NetworkException
import net.primal.domain.wallet.model.WalletBalanceResult

interface WalletService {
    @Throws(NetworkException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun fetchWalletBalance(wallet: Wallet): WalletBalanceResult
}
