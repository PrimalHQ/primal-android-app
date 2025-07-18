package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.domain.wallet.WalletType

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

    @Query("SELECT walletId FROM ActiveWalletData WHERE userId = :userId")
    fun observeActiveWalletId(userId: String): Flow<String?>

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

    @Query(
        """
        SELECT * FROM WalletInfo
        WHERE userId = :userId AND type = :type
        ORDER BY lastUpdatedAt DESC LIMIT 1
        """,
    )
    suspend fun findLastUsedWalletByType(userId: String, type: WalletType): Wallet?
}
