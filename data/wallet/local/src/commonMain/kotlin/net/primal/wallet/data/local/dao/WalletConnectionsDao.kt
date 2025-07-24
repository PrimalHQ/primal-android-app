package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WalletConnectionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRemoteKeys(data: List<WalletTransactionRemoteKey>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrossRefs(data: List<WalletTransactionCrossRef>)

    @Query("SELECT * FROM WalletTransactionRemoteKey WHERE transactionId = :transactionId AND walletId = :walletId")
    suspend fun findByTransactionId(transactionId: String, walletId: String): WalletTransactionRemoteKey?

    @Query("""
        SELECT * FROM WalletTransactionRemoteKey
        WHERE walletId = :walletId
        ORDER BY cachedAt DESC LIMIT 1
        """)
    suspend fun findLatestRemoteKeyByWalletId(walletId: String): WalletTransactionRemoteKey?

    @Query("""
        SELECT * FROM WalletTransactionCrossRef
        WHERE walletId = :walletId
        ORDER BY position DESC LIMIT 1
        """)
    suspend fun findLatestCrossRefByWalletId(walletId: String): WalletTransactionCrossRef?

    @Query("DELETE FROM WalletTransactionRemoteKey WHERE walletId = :walletId")
    suspend fun deleteAllRemoteKeysByWalletId(walletId: String)

    @Query("DELETE FROM WalletTransactionCrossRef WHERE walletId = :walletId")
    suspend fun deleteAllCrossRefsByWalletId(walletId: String)
}
