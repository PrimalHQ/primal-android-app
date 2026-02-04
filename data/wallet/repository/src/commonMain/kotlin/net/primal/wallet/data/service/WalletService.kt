package net.primal.wallet.data.service

import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.rates.fees.OnChainTransactionFeeTier
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult

internal interface WalletService<W : Wallet> {
    suspend fun fetchWalletBalance(wallet: W): Result<WalletBalanceResult>

    suspend fun subscribeToWalletBalance(wallet: W): Flow<WalletBalanceResult>

    suspend fun fetchTransactions(wallet: W, request: TransactionsRequest): Result<List<Transaction>>

    suspend fun createLightningInvoice(wallet: W, request: LnInvoiceCreateRequest): Result<LnInvoiceCreateResult>

    suspend fun createOnChainAddress(wallet: W): Result<OnChainAddressResult>

    suspend fun pay(wallet: W, request: TxRequest): Result<Unit>

    suspend fun fetchMiningFees(
        wallet: W,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<List<OnChainTransactionFeeTier>>

    /**
     * Awaits confirmation that a Lightning invoice has been paid.
     */
    suspend fun awaitInvoicePayment(
        wallet: W,
        invoice: String,
        timeout: Duration,
    ): Result<Unit>
}
