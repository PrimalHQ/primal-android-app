package net.primal.wallet.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import net.primal.domain.wallet.TxType

@Dao
interface WalletTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<WalletTransactionData>)

    @Query(
        """
            SELECT * FROM WalletTransactionData
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED") AND walletId IS :walletId
            ORDER BY updatedAt DESC
        """,
    )
    fun latestTransactionsPagedByWalletId(walletId: String): PagingSource<Int, WalletTransactionData>

    @Query(
        """
            SELECT * FROM WalletTransactionData
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED") AND walletId IS :walletId
            ORDER BY updatedAt DESC LIMIT :limit
        """,
    )
    suspend fun latestTransactionsByWalletId(walletId: String, limit: Int): List<WalletTransactionData>

    @Transaction
    @Query(
        """
            SELECT * FROM WalletTransactionData
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED")
                AND walletId IS :walletId
                AND (:type IS NULL OR type IS :type)
                AND (:from IS NULL OR createdAt >= :from)
                AND (:until IS NULL OR createdAt <= :until)
            ORDER BY createdAt DESC
            LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun queryTransactions(
        walletId: String,
        type: TxType?,
        limit: Int,
        offset: Int,
        from: Long?,
        until: Long?,
    ): List<WalletTransactionData>

    @Transaction
    @Query(
        """
            SELECT * FROM WalletTransactionData
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED") AND walletId IS :walletId
            ORDER BY createdAt DESC
        """,
    )
    suspend fun allTransactionsByWalletId(walletId: String): List<WalletTransactionData>

    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId ORDER BY updatedAt ASC LIMIT 1")
    suspend fun firstByWalletId(walletId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun lastByWalletId(walletId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE transactionId IS :txId")
    suspend fun findTransactionById(txId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE invoice = :invoice LIMIT 1")
    suspend fun findByInvoice(invoice: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE paymentHash = :paymentHash LIMIT 1")
    suspend fun findByPaymentHash(paymentHash: String): WalletTransactionData?

    @Query(
        """
        SELECT * FROM WalletTransactionData
        WHERE walletId IS :walletId AND state = 'CREATED' AND txKind = 'ON_CHAIN'
        """,
    )
    suspend fun findCreatedOnChain(walletId: String): List<WalletTransactionData>

    @Query("DELETE FROM WalletTransactionData WHERE transactionId IS :transactionId")
    suspend fun deleteByTransactionId(transactionId: String)

    @Query("DELETE FROM WalletTransactionData WHERE walletId IS :walletId")
    suspend fun deleteByWalletId(walletId: String)

    @Query("DELETE FROM WalletTransactionData WHERE userId IS :userId")
    suspend fun deleteAllTransactions(userId: String)
}
