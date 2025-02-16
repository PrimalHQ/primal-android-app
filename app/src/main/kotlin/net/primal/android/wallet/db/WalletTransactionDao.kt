package net.primal.android.wallet.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface WalletTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<WalletTransactionData>)

    @Transaction
    @Query(
        """
            SELECT * FROM WalletTransactionData 
            WHERE state IN ("SUCCEEDED", "PROCESSING", "CREATED") AND userId IS :userId
            ORDER BY updatedAt DESC
        """,
    )
    fun latestTransactionsPagedByUserId(userId: String): PagingSource<Int, WalletTransaction>

    @Query("SELECT * FROM WalletTransactionData WHERE userId IS :userId ORDER BY updatedAt DESC LIMIT 1")
    fun firstByUserId(userId: String): WalletTransactionData?

    @Query("SELECT * FROM WalletTransactionData WHERE userId IS :userId ORDER BY updatedAt ASC LIMIT 1")
    fun lastByUserId(userId: String): WalletTransactionData?

    @Transaction
    @Query("SELECT * FROM WalletTransactionData WHERE id IS :txId")
    fun findTransactionById(txId: String): WalletTransaction?

    @Query("DELETE FROM WalletTransactionData WHERE userId IS :userId")
    fun deleteAllTransactionsUserId(userId: String)
}
