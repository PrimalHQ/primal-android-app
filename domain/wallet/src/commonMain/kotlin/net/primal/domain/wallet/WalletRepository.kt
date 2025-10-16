package net.primal.domain.wallet

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.transactions.Transaction

interface WalletRepository {

    suspend fun getWalletById(walletId: String): Result<Wallet>

    suspend fun deleteWalletById(walletId: String)

    suspend fun upsertNostrWallet(userId: String, wallet: Wallet.NWC)

    suspend fun upsertWalletSettings(walletId: String, spamThresholdAmountInSats: Long)

    suspend fun fetchWalletBalance(walletId: String): Result<Unit>

    suspend fun updateWalletBalance(
        walletId: String,
        balanceInBtc: Double,
        maxBalanceInBtc: Double?,
    )

    fun latestTransactions(walletId: String): Flow<PagingData<Transaction>>

    suspend fun findTransactionByIdOrNull(txId: String): Transaction?

    suspend fun deleteAllUserData(userId: String)

    suspend fun pay(walletId: String, request: TxRequest): Result<Unit>

    suspend fun createLightningInvoice(
        walletId: String,
        amountInBtc: String?,
        comment: String?,
    ): Result<LnInvoiceCreateResult>

    suspend fun createOnChainAddress(userId: String): OnChainAddressResult

    suspend fun parseLnUrl(userId: String, lnurl: String): LnUrlParseResult

    suspend fun parseLnInvoice(userId: String, lnbc: String): LnInvoiceParseResult
}
