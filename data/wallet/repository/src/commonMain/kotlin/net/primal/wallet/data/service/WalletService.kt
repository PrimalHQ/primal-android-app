package net.primal.wallet.data.service

import net.primal.core.utils.Result
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.model.CreateLightningInvoiceRequest
import net.primal.wallet.data.model.Transaction
import net.primal.wallet.data.model.TransactionsRequest

internal interface WalletService {
    suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult>

    suspend fun fetchTransactions(wallet: Wallet, request: TransactionsRequest): Result<List<Transaction>>

    suspend fun createLightningInvoice(
        wallet: Wallet,
        request: CreateLightningInvoiceRequest,
    ): Result<LnInvoiceCreateResult>
}
