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

    @Query("INSERT OR IGNORE INTO WalletUserLink (userId, walletId) VALUES (:userId, :walletId)")
    suspend fun insertWalletUserLink(userId: String, walletId: String)

    @Transaction
    @Query(
        """
        SELECT wi.* FROM WalletInfo wi
        INNER JOIN WalletUserLink wul ON wi.walletId = wul.walletId
        WHERE wul.userId = :userId
        """,
    )
    fun observeWalletsByUserId(userId: String): Flow<List<Wallet>>

    @Suppress("FunctionName")
    @Query(
        """
        UPDATE WalletUserLink SET lightningAddress = NULL
        WHERE lightningAddress = :lightningAddress
        """,
    )
    suspend fun _nullOutLightningAddress(lightningAddress: Encryptable<String>)

    @Suppress("FunctionName")
    @Query(
        """
        UPDATE WalletUserLink SET lightningAddress = :lightningAddress
        WHERE userId = :userId AND walletId = :walletId
        """,
    )
    suspend fun _setLightningAddress(
        userId: String,
        walletId: String,
        lightningAddress: Encryptable<String>,
    )

    @Transaction
    suspend fun assignLightningAddress(
        userId: String,
        walletId: String,
        lightningAddress: Encryptable<String>,
    ) {
        _nullOutLightningAddress(lightningAddress)
        _setLightningAddress(userId, walletId, lightningAddress)
    }

    @Query("SELECT * FROM WalletUserLink WHERE userId = :userId AND walletId = :walletId")
    suspend fun findWalletUserLink(userId: String, walletId: String): WalletUserLink?

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

    @Query(
        """
        SELECT swd.* FROM SparkWalletData swd
        INNER JOIN WalletUserLink wul ON swd.walletId = wul.walletId
        WHERE wul.userId = :userId
        """,
    )
    suspend fun findAllSparkWalletDataByUserId(userId: String): List<SparkWalletData>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM SparkWalletData swd
            INNER JOIN WalletUserLink wul ON swd.walletId = wul.walletId
            WHERE wul.userId = :userId
        )
        """,
    )
    suspend fun hasSparkWalletDataByUserId(userId: String): Boolean

    @Query(
        """
        SELECT wul.walletId FROM WalletUserLink wul
        INNER JOIN WalletInfo wi ON wul.walletId = wi.walletId
        WHERE wul.userId = :userId
          AND wi.type = 'SPARK'
          AND wul.lightningAddress IS NOT NULL
        LIMIT 1
        """,
    )
    suspend fun findRegisteredSparkWalletId(userId: String): String?

    @Query("UPDATE SparkWalletData SET backedUp = :backedUp WHERE walletId = :walletId")
    suspend fun updateSparkWalletBackedUp(walletId: String, backedUp: Boolean)

    @Query("UPDATE SparkWalletData SET primalTxsMigrated = :migrated WHERE walletId = :walletId")
    suspend fun updatePrimalTxsMigrated(walletId: String, migrated: Boolean)

    @Query("UPDATE SparkWalletData SET primalTxsMigratedUntil = :until WHERE walletId = :walletId")
    suspend fun updatePrimalTxsMigratedUntil(walletId: String, until: Long?)

    @Query(
        """
        UPDATE SparkWalletData
        SET primalTxsMigrated = NULL, primalTxsMigratedUntil = NULL
        WHERE walletId = :walletId
        """,
    )
    suspend fun clearPrimalTxsMigrationState(walletId: String)

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

    @Query("UPDATE WalletInfo SET lastUpdatedAt = CAST(strftime('%s','now') AS INTEGER) WHERE walletId = :walletId")
    suspend fun touchLastUpdatedAt(walletId: String)

    @Query("SELECT * FROM WalletInfo WHERE walletId = :walletId")
    suspend fun findWalletInfo(walletId: String): WalletInfo?

    @Query(
        """
        SELECT wi.* FROM WalletInfo wi
        INNER JOIN WalletUserLink wul ON wi.walletId = wul.walletId
        WHERE wul.userId = :userId
        """,
    )
    suspend fun findWalletInfosByUserId(userId: String): List<WalletInfo>

    @Transaction
    @Query("SELECT * FROM WalletInfo WHERE walletId = :walletId")
    suspend fun findWallet(walletId: String): Wallet?

    @Transaction
    @Query(
        """
        SELECT wi.* FROM WalletInfo wi
        INNER JOIN WalletUserLink wul ON wi.walletId = wul.walletId
        WHERE wul.userId = :userId AND wi.type = :type
        ORDER BY wi.lastUpdatedAt DESC LIMIT 1
        """,
    )
    suspend fun findLastUsedWalletByType(userId: String, type: WalletType): Wallet?

    @Transaction
    @Query(
        """
        SELECT wi.* FROM WalletInfo wi
        INNER JOIN WalletUserLink wul ON wi.walletId = wul.walletId
        WHERE wul.userId = :userId AND wi.type in (:type)
        ORDER BY wi.lastUpdatedAt DESC LIMIT 1
        """,
    )
    suspend fun findLastUsedWalletByTypes(userId: String, type: Set<WalletType>): Wallet?

    @Transaction
    suspend fun deleteWalletsByIds(walletIds: List<String>) {
        _deleteWalletInfosByIds(walletIds)
        _deletePrimalWalletDataByIds(walletIds)
        _deleteNostrWalletDataByIds(walletIds)
        _deleteSparkWalletDataByIds(walletIds)
        _deleteWalletUserLinksByIds(walletIds)
        _deleteActiveWalletDataByIds(walletIds)
    }

    @Suppress("FunctionName")
    @Query("DELETE FROM WalletInfo WHERE walletId IN (:walletIds)")
    suspend fun _deleteWalletInfosByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM PrimalWalletData WHERE walletId IN (:walletIds)")
    suspend fun _deletePrimalWalletDataByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM NostrWalletData WHERE walletId IN (:walletIds)")
    suspend fun _deleteNostrWalletDataByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM SparkWalletData WHERE walletId IN (:walletIds)")
    suspend fun _deleteSparkWalletDataByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM WalletUserLink WHERE walletId IN (:walletIds)")
    suspend fun _deleteWalletUserLinksByIds(walletIds: List<String>)

    @Suppress("FunctionName")
    @Query("DELETE FROM ActiveWalletData WHERE walletId IN (:walletIds)")
    suspend fun _deleteActiveWalletDataByIds(walletIds: List<String>)
}
