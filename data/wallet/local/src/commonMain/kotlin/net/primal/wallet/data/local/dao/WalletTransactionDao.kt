package net.primal.wallet.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface WalletTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<WalletTransactionData>)

    @Query(
        """
            SELECT * FROM WalletTransactionData 
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED") AND userId IS :userId
            ORDER BY updatedAt DESC
        """,
    )
    fun latestTransactionsPagedByUserId(userId: String): PagingSource<Int, WalletTransactionData>

    @Query("SELECT * FROM WalletTransactionData WHERE userId IS :userId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun firstByUserId(userId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE userId IS :userId ORDER BY updatedAt ASC LIMIT 1")
    suspend fun lastByUserId(userId: String): WalletTransactionData?

    @Transaction
    @Query("SELECT * FROM WalletTransactionData WHERE id IS :txId")
    suspend fun findTransactionById(txId: String): WalletTransactionData?

    @Query("DELETE FROM WalletTransactionData WHERE userId IS :userId")
    suspend fun deleteAllTransactionsByUserId(userId: String)
}
