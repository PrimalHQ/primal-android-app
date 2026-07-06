package net.primal.wallet.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

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
