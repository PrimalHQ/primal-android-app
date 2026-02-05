package net.primal.domain.wallet

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.model.WalletBalanceResult

interface WalletRepository {

    suspend fun getWalletById(walletId: String): Result<Wallet>

    suspend fun deleteWalletById(walletId: String)

    suspend fun upsertNostrWallet(userId: String, wallet: Wallet.NWC)

    suspend fun upsertWalletSettings(walletId: String, spamThresholdAmountInSats: Long)

    suspend fun fetchWalletBalance(walletId: String): Result<Unit>

    suspend fun subscribeToWalletBalance(walletId: String): Flow<WalletBalanceResult>

    suspend fun updateWalletBalance(
        walletId: String,
        balanceInBtc: Double,
        maxBalanceInBtc: Double?,
    )

    fun latestTransactions(walletId: String): Flow<PagingData<Transaction>>

    suspend fun latestTransactions(walletId: String, limit: Int): List<Transaction>

    suspend fun allTransactions(walletId: String): List<Transaction>

    suspend fun queryTransactions(
        walletId: String,
        type: TxType?,
        limit: Int,
        offset: Int,
        from: Long? = null,
        until: Long? = null,
    ): List<Transaction>

    suspend fun findTransactionByIdOrNull(txId: String): Transaction?

    suspend fun findTransactionByInvoice(invoice: String): Transaction?

    suspend fun findTransactionByPaymentHash(paymentHash: String): Transaction?

    suspend fun deleteAllTransactions(userId: String)

    suspend fun deleteAllUserData(userId: String)

    suspend fun pay(walletId: String, request: TxRequest): Result<Unit>

    suspend fun createLightningInvoice(
        walletId: String,
        amountInBtc: String?,
        comment: String?,
    ): Result<LnInvoiceCreateResult>

    suspend fun createOnChainAddress(walletId: String): Result<OnChainAddressResult>

    suspend fun parseLnUrl(userId: String, lnurl: String): LnUrlParseResult

    suspend fun parseLnInvoice(userId: String, lnbc: String): LnInvoiceParseResult

    suspend fun persistNwcInvoice(nwcInvoice: NwcInvoice)

    suspend fun findNwcInvoiceByPaymentHash(paymentHash: String): NwcInvoice?

    suspend fun findNwcInvoiceByInvoice(invoice: String): NwcInvoice?

    suspend fun markNwcInvoiceExpired(invoice: String)
}
