package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActiveWallet(data: ActiveWalletData)

    @Query("DELETE FROM ActiveWalletData WHERE userId = :userId")
    suspend fun clearActiveWallet(userId: String)

    @Query("SELECT * FROM ActiveWalletData WHERE userId = :userId")
    suspend fun getActiveWallet(userId: String): ActiveWallet?

    @Transaction
    @Query("SELECT * FROM ActiveWalletData WHERE userId = :userId")
    fun observeActiveWallet(userId: String): Flow<ActiveWallet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWalletInfo(info: WalletInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrimalWalletData(data: PrimalWalletData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNostrWalletData(data: NostrWalletData)

    @Query(
        """
        UPDATE WalletInfo 
        SET balanceInBtc = :balanceInBtc, maxBalanceInBtc = :maxBalanceInBtc,
         lastUpdatedAt = CAST(strftime('%s','now') AS INTEGER) 
        WHERE walletId = :walletId
        """,
    )
    suspend fun updateWalletBalance(
        walletId: String,
        balanceInBtc: Double,
        maxBalanceInBtc: Double?,
    )

    @Query("SELECT * FROM WalletInfo WHERE walletId = :walletId")
    suspend fun findWalletInfo(walletId: String): WalletInfo?

    @Query("SELECT * FROM WalletInfo WHERE walletId = :walletId")
    suspend fun findWallet(walletId: String): Wallet?
}
