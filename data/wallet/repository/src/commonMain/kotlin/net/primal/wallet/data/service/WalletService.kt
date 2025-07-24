package net.primal.wallet.data.service

import net.primal.core.utils.Result
import net.primal.domain.wallet.Wallet
import net.primal.wallet.data.model.TransactionsRequest
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.model.Transaction

internal interface WalletService {
    suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult>

    suspend fun fetchTransactions(wallet: Wallet, request: TransactionsRequest): Result<List<Transaction>>
}
