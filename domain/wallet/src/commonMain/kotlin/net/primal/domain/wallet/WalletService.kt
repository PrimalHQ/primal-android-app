package net.primal.domain.wallet

import net.primal.core.utils.Result
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.model.TransactionsRequest
import net.primal.domain.wallet.model.WalletBalanceResult

interface WalletService {
    suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult>

    suspend fun fetchTransactions(wallet: Wallet, request: TransactionsRequest): Result<List<Transaction>>
}
