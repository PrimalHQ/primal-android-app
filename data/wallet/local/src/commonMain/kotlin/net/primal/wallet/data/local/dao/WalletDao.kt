package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.Encryptable

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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreWalletInfo(info: WalletInfo)

    @Transaction
    @Query("SELECT * FROM WalletInfo WHERE userId = :userId")
    fun observeWalletsByUserId(userId: String): Flow<List<Wallet>>

    @Query("UPDATE WalletInfo SET lightningAddress = :lightningAddress WHERE walletId = :walletId")
    suspend fun updateWalletLightningAddress(walletId: String, lightningAddress: Encryptable<String>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWalletInfo(info: WalletInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrimalWalletData(data: PrimalWalletData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNostrWalletData(data: NostrWalletData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSparkWalletData(data: SparkWalletData)

    @Query("SELECT * FROM SparkWalletData WHERE walletId = :walletId")
    suspend fun findSparkWalletData(walletId: String): SparkWalletData?

    @Query("SELECT * FROM SparkWalletData WHERE userId = :userId")
    suspend fun findAllSparkWalletDataByUserId(userId: String): List<SparkWalletData>

    @Query("UPDATE SparkWalletData SET backedUp = :backedUp WHERE walletId = :walletId")
    suspend fun updateSparkWalletBackedUp(walletId: String, backedUp: Boolean)

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
        balanceInBtc: Encryptable<Double>,
        maxBalanceInBtc: Encryptable<Double>?,
    )

    @Suppress("FunctionName")
    @Query("DELETE FROM WalletInfo WHERE walletId IN (:walletIds)")
    suspend fun _deleteWalletInfosByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM PrimalWalletData WHERE walletId IN (:walletIds)")
    suspend fun _deletePrimalWalletsByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM NostrWalletData WHERE walletId IN (:walletIds)")
    suspend fun _deleteNostrWalletsByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM SparkWalletData WHERE walletId IN (:walletIds)")
    suspend fun _deleteSparkWalletsByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM WalletInfo WHERE userId = :userId AND type = 'SPARK'")
    suspend fun _deleteSparkWalletInfoByUserId(userId: String)

    @Suppress("FunctionName")
    @Query("DELETE FROM SparkWalletData WHERE userId = :userId")
    suspend fun _deleteSparkWalletDataByUserId(userId: String)

    @Transaction
    suspend fun deleteSparkWalletByUserId(userId: String): String? {
        val walletId = findLastUsedWalletByType(userId = userId, type = WalletType.SPARK)
        _deleteSparkWalletInfoByUserId(userId = userId)
        _deleteSparkWalletDataByUserId(userId = userId)

        return walletId?.info?.walletId
    }

    @Transaction
    suspend fun deleteWalletsByIds(walletIds: List<String>) {
        _deleteWalletInfosByIds(walletIds = walletIds)
        _deleteNostrWalletsByIds(walletIds = walletIds)
        _deletePrimalWalletsByIds(walletIds = walletIds)
        _deleteSparkWalletsByIds(walletIds = walletIds)
    }

    @Query("SELECT * FROM WalletInfo WHERE walletId = :walletId")
    suspend fun findWalletInfo(walletId: String): WalletInfo?

    @Query("SELECT * FROM WalletInfo WHERE userId = :userId")
    suspend fun findWalletInfosByUserId(userId: String): List<WalletInfo>

    @Transaction
    @Query("SELECT * FROM WalletInfo WHERE walletId = :walletId")
    suspend fun findWallet(walletId: String): Wallet?

    @Transaction
    @Query(
        """
        SELECT * FROM WalletInfo
        WHERE userId = :userId AND type = :type
        ORDER BY lastUpdatedAt DESC LIMIT 1
        """,
    )
    suspend fun findLastUsedWalletByType(userId: String, type: WalletType): Wallet?

    @Transaction
    @Query(
        """
        SELECT * FROM WalletInfo
        WHERE userId = :userId AND type in (:type)
        ORDER BY lastUpdatedAt DESC LIMIT 1
        """,
    )
    suspend fun findLastUsedWalletByTypes(userId: String, type: Set<WalletType>): Wallet?
}
