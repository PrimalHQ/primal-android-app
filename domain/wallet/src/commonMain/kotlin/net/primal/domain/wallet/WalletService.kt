package net.primal.domain.wallet

import net.primal.core.utils.Result
import net.primal.domain.wallet.model.WalletBalanceResult

interface WalletService {
    suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult>
}
