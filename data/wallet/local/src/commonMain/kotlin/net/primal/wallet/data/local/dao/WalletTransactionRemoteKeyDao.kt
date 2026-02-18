package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface WalletTransactionRemoteKeyDao {

    @Query(
        """
        SELECT * FROM WalletTransactionRemoteKey
        WHERE walletId = :walletId AND transactionId = :transactionId
        """,
    )
    suspend fun find(walletId: String, transactionId: String): WalletTransactionRemoteKey?

    @Upsert
    suspend fun upsert(data: List<WalletTransactionRemoteKey>)

    @Query("DELETE FROM WalletTransactionRemoteKey WHERE walletId = :walletId")
    suspend fun deleteByWalletId(walletId: String)

    @Query("DELETE FROM WalletTransactionRemoteKey WHERE walletId in (:walletIds)")
    suspend fun deleteByWalletIds(walletIds: List<String>)
}
