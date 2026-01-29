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
            ORDER BY updatedAt DESC
            LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun queryTransactions(
        walletId: String,
        type: TxType?,
        limit: Int,
        offset: Int,
    ): List<WalletTransactionData>

    @Transaction
    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId AND invoice = :invoice")
    suspend fun findTransactionByInvoice(walletId: String, invoice: Encryptable<String>): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId ORDER BY updatedAt ASC LIMIT 1")
    suspend fun firstByWalletId(walletId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE walletId IS :walletId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun lastByWalletId(walletId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE transactionId IS :txId")
    suspend fun findTransactionById(txId: String): WalletTransactionData?

    @Query("DELETE FROM WalletTransactionData WHERE userId IS :userId")
    suspend fun deleteAllTransactions(userId: String)
}
