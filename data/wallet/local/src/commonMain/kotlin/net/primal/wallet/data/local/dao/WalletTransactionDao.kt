package net.primal.wallet.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import net.primal.domain.wallet.TxType
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface WalletTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<WalletTransactionData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllPrimalTransactions(data: List<PrimalTransactionData>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllNostrTransactions(data: List<NostrTransactionData>)

    @Transaction
    @Query(
        """
            SELECT * FROM WalletTransactionData 
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED") AND walletId IS :walletId
            ORDER BY updatedAt DESC
        """,
    )
    fun latestTransactionsPagedByWalletId(walletId: String): PagingSource<Int, WalletTransaction>

    @Transaction
    @Query(
        """
            SELECT * FROM WalletTransactionData 
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED") AND walletId IS :walletId
            ORDER BY updatedAt DESC LIMIT :limit
        """,
    )
    suspend fun latestTransactionsByWalletId(walletId: String, limit: Int): List<WalletTransaction>

    @Transaction
    @Query(
        """
            SELECT * FROM WalletTransactionData
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED")
                AND walletId IS :walletId
                AND (:type IS NULL OR type IS :type)
            ORDER BY updatedAt DESC
            LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun queryTransactions(
        walletId: String,
        type: TxType?,
        limit: Int,
        offset: Int,
    ): List<WalletTransaction>

    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId ORDER BY updatedAt ASC LIMIT 1")
    suspend fun firstByWalletId(walletId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun lastByWalletId(walletId: String): WalletTransactionData?

    @Transaction
    @Query("SELECT * FROM WalletTransactionData WHERE transactionId IS :txId")
    suspend fun findTransactionById(txId: String): WalletTransaction?

    @Transaction
    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId AND invoice = :invoice")
    suspend fun findTransactionByInvoice(walletId: String, invoice: Encryptable<String>): WalletTransaction?

    @Transaction
    suspend fun deleteAllTransactions(userId: Encryptable<String>) {
        val txIds = _findTransactionIdsByUserId(userId)
        if (txIds.isNotEmpty()) {
            _deletePrimalTransactionDataByIds(txIds)
            _deleteNostrTransactionDataByIds(txIds)
        }
        _deleteAllTransactionDataByUserId(userId)
    }

    @Suppress("FunctionName")
    @Query("SELECT transactionId FROM WalletTransactionData WHERE userId = :userId")
    suspend fun _findTransactionIdsByUserId(userId: Encryptable<String>): List<String>

    @Suppress("FunctionName")
    @Query("DELETE FROM WalletTransactionData WHERE userId IS :userId")
    suspend fun _deleteAllTransactionDataByUserId(userId: Encryptable<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM PrimalTransactionData WHERE transactionId IN (:transactionIds)")
    suspend fun _deletePrimalTransactionDataByIds(transactionIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM NostrTransactionData WHERE transactionId IN (:transactionIds)")
    suspend fun _deleteNostrTransactionDataByIds(transactionIds: List<String>)
}
