package net.primal.domain.wallet

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface WalletRepository {

    suspend fun fetchWalletBalance(userId: String)

    fun latestTransactions(userId: String): Flow<PagingData<TransactionWithProfile>>

    suspend fun findTransactionByIdOrNull(txId: String): TransactionWithProfile?

    suspend fun deleteAllTransactions(userId: String)

    suspend fun pay(params: WalletPayParams)

    suspend fun createLightningInvoice(
        userId: String,
        amountInBtc: String?,
        comment: String?,
    ): LnInvoiceCreateResult

    suspend fun createOnChainAddress(userId: String): OnChainAddressResult

    suspend fun parseLnUrl(userId: String, lnurl: String): LnUrlParseResult

    suspend fun parseLnInvoice(userId: String, lnbc: String): LnInvoiceParseResult
}
