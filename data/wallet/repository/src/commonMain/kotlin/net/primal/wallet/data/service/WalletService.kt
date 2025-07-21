package net.primal.wallet.data.service

import net.primal.core.utils.Result
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult

interface WalletService {
    suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult>
}
