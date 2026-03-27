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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWalletUserLink(link: WalletUserLink)

    @Transaction
    @Query(
        """
        SELECT wi.* FROM WalletInfo wi
        INNER JOIN WalletUserLink wul ON wi.walletId = wul.walletId
        WHERE wul.userId = :userId
        """,
    )
    fun observeWalletsByUserId(userId: String): Flow<List<Wallet>>

    @Query(
        """
        UPDATE WalletUserLink SET lightningAddress = :lightningAddress
        WHERE userId = :userId AND walletId = :walletId
        """,
    )
    suspend fun updateLinkLightningAddress(
        userId: String,
        walletId: String,
        lightningAddress: Encryptable<String>?,
    )

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
}
