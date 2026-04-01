package net.primal.wallet.data.local.db

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import net.primal.shared.data.local.serialization.EncryptableTypeConverters
import net.primal.shared.data.local.serialization.ListsTypeConverters
import net.primal.wallet.data.local.dao.ActiveWalletData
import net.primal.wallet.data.local.dao.NostrWalletData
import net.primal.wallet.data.local.dao.PrimalWalletData
import net.primal.wallet.data.local.dao.ReceiveRequestDao
import net.primal.wallet.data.local.dao.ReceiveRequestData
import net.primal.wallet.data.local.dao.SparkWalletData
import net.primal.wallet.data.local.dao.UserWalletPreferences
import net.primal.wallet.data.local.dao.UserWalletPreferencesDao
import net.primal.wallet.data.local.dao.WalletDao
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.dao.WalletSettings
import net.primal.wallet.data.local.dao.WalletSettingsDao
import net.primal.wallet.data.local.dao.WalletTransactionDao
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.dao.WalletTransactionRemoteKey
import net.primal.wallet.data.local.dao.WalletTransactionRemoteKeyDao
import net.primal.wallet.data.local.dao.WalletUserLink
import net.primal.wallet.data.local.dao.ZapEnrichmentTracker
import net.primal.wallet.data.local.dao.ZapEnrichmentTrackerDao
import net.primal.wallet.data.local.dao.nwc.NwcConnectionDao
import net.primal.wallet.data.local.dao.nwc.NwcConnectionData
import net.primal.wallet.data.local.dao.nwc.NwcDailyBudgetData
import net.primal.wallet.data.local.dao.nwc.NwcInvoiceDao
import net.primal.wallet.data.local.dao.nwc.NwcInvoiceData
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldDao
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldData
import net.primal.wallet.data.local.dao.nwc.NwcPendingEventDao
import net.primal.wallet.data.local.dao.nwc.NwcPendingEventData
import net.primal.wallet.data.local.dao.nwc.NwcWalletRequestLog
import net.primal.wallet.data.local.dao.nwc.NwcWalletRequestLogDao

@Database(
    entities = [
        WalletInfo::class,
        NostrWalletData::class,
        PrimalWalletData::class,
        SparkWalletData::class,
        ActiveWalletData::class,
        WalletTransactionData::class,
        WalletTransactionRemoteKey::class,
        WalletSettings::class,
        NwcConnectionData::class,
        NwcPaymentHoldData::class,
        NwcDailyBudgetData::class,
        NwcWalletRequestLog::class,
        NwcInvoiceData::class,
        NwcPendingEventData::class,
        ReceiveRequestData::class,
        ZapEnrichmentTracker::class,
        WalletUserLink::class,
        UserWalletPreferences::class,
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ],
)
@TypeConverters(ListsTypeConverters::class, EncryptableTypeConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun wallet(): WalletDao
    abstract fun walletTransactions(): WalletTransactionDao
    abstract fun walletTransactionRemoteKeys(): WalletTransactionRemoteKeyDao
    abstract fun walletSettings(): WalletSettingsDao
    abstract fun nwcConnections(): NwcConnectionDao
    abstract fun nwcPaymentHolds(): NwcPaymentHoldDao
    abstract fun nwcInvoices(): NwcInvoiceDao
    abstract fun nwcPendingEvents(): NwcPendingEventDao
    abstract fun nwcLogs(): NwcWalletRequestLogDao
    abstract fun receiveRequests(): ReceiveRequestDao
    abstract fun zapEnrichmentTracker(): ZapEnrichmentTrackerDao
    abstract fun userWalletPreferences(): UserWalletPreferencesDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                // Drop tables that will be re-fetched from server.
                // NostrWalletData, ActiveWalletData, WalletInfo, WalletSettings preserved
                // (can't be re-fetched, and their schemas are compatible).
                connection.execSQL("DROP TABLE IF EXISTS WalletTransactionData")
                connection.execSQL("DROP TABLE IF EXISTS NostrTransactionData")
                connection.execSQL("DROP TABLE IF EXISTS PrimalTransactionData")
                connection.execSQL("DROP TABLE IF EXISTS PrimalWalletData")

                // Convert WalletInfo.userId from Encryptable<String> to plain String
                val stmt = connection.prepare("SELECT walletId, userId FROM WalletInfo")
                try {
                    while (stmt.step()) {
                        val walletId = stmt.getText(0)
                        val encryptedUserId = stmt.getText(1)
                        val decrypted = EncryptableTypeConverters.toString(encryptedUserId)?.decrypted
                        if (decrypted != null) {
                            val update = connection.prepare(
                                "UPDATE WalletInfo SET userId = ? WHERE walletId = ?",
                            )
                            try {
                                update.bindText(1, decrypted)
                                update.bindText(2, walletId)
                                update.step()
                            } finally {
                                update.close()
                            }
                        }
                    }
                } finally {
                    stmt.close()
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                // Create new tables
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS WalletUserLink (
                        userId TEXT NOT NULL,
                        walletId TEXT NOT NULL,
                        lightningAddress TEXT,
                        PRIMARY KEY (userId, walletId)
                    )
                    """,
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_WalletUserLink_walletId ON WalletUserLink (walletId)",
                )
                connection.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "index_WalletUserLink_lightningAddress ON WalletUserLink (lightningAddress)",
                )
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS UserWalletPreferences (
                        userId TEXT NOT NULL,
                        nwcAutoStart INTEGER NOT NULL DEFAULT 1,
                        PRIMARY KEY (userId)
                    )
                    """,
                )

                // Populate new tables from existing data
                connection.execSQL(
                    "INSERT OR IGNORE INTO WalletUserLink (userId, walletId, lightningAddress) " +
                        "SELECT userId, walletId, lightningAddress FROM WalletInfo",
                )
                connection.execSQL(
                    "INSERT OR IGNORE INTO UserWalletPreferences (userId, nwcAutoStart) " +
                        "SELECT userId, nwcAutoStart FROM SparkWalletData",
                )

                // Recreate WalletInfo without userId
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS WalletInfo_new (
                        walletId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        balanceInBtc TEXT,
                        maxBalanceInBtc TEXT,
                        lastUpdatedAt INTEGER,
                        PRIMARY KEY(walletId)
                    )
                    """,
                )
                connection.execSQL(
                    """
                    INSERT INTO WalletInfo_new (walletId, type, balanceInBtc, maxBalanceInBtc, lastUpdatedAt)
                    SELECT walletId, type, balanceInBtc, maxBalanceInBtc, lastUpdatedAt FROM WalletInfo
                    """,
                )
                connection.execSQL("DROP TABLE WalletInfo")
                connection.execSQL("ALTER TABLE WalletInfo_new RENAME TO WalletInfo")

                // Recreate SparkWalletData without userId and nwcAutoStart
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS SparkWalletData_new (
                        walletId TEXT NOT NULL,
                        seedWords TEXT NOT NULL,
                        backedUp INTEGER NOT NULL,
                        primalTxsMigrated INTEGER,
                        primalTxsMigratedUntil INTEGER,
                        PRIMARY KEY(walletId)
                    )
                    """,
                )
                connection.execSQL(
                    """
                    INSERT INTO SparkWalletData_new (walletId, seedWords, backedUp, primalTxsMigrated, primalTxsMigratedUntil)
                    SELECT walletId, seedWords, backedUp, primalTxsMigrated, primalTxsMigratedUntil FROM SparkWalletData
                    """,
                )
                connection.execSQL("DROP TABLE SparkWalletData")
                connection.execSQL("ALTER TABLE SparkWalletData_new RENAME TO SparkWalletData")

                // Recreate WalletTransactionData without userId
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS WalletTransactionData_new (
                        transactionId TEXT NOT NULL,
                        walletId TEXT NOT NULL,
                        walletType TEXT NOT NULL,
                        type TEXT NOT NULL,
                        state TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        completedAt TEXT,
                        note TEXT,
                        invoice TEXT,
                        amountInBtc TEXT NOT NULL,
                        totalFeeInBtc TEXT,
                        otherUserId TEXT,
                        zappedEntity TEXT,
                        zappedByUserId TEXT,
                        txKind TEXT NOT NULL,
                        onChainAddress TEXT,
                        onChainTxId TEXT,
                        preimage TEXT,
                        paymentHash TEXT,
                        amountInUsd TEXT,
                        exchangeRate TEXT,
                        otherLightningAddress TEXT,
                        PRIMARY KEY(transactionId)
                    )
                    """,
                )
                connection.execSQL(
                    """
                    INSERT INTO WalletTransactionData_new (
                        transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                        completedAt, note, invoice, amountInBtc, totalFeeInBtc, otherUserId,
                        zappedEntity, zappedByUserId, txKind, onChainAddress, onChainTxId,
                        preimage, paymentHash, amountInUsd, exchangeRate, otherLightningAddress
                    )
                    SELECT
                        transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                        completedAt, note, invoice, amountInBtc, totalFeeInBtc, otherUserId,
                        zappedEntity, zappedByUserId, txKind, onChainAddress, onChainTxId,
                        preimage, paymentHash, amountInUsd, exchangeRate, otherLightningAddress
                    FROM WalletTransactionData
                    """,
                )
                connection.execSQL("DROP TABLE WalletTransactionData")
                connection.execSQL("ALTER TABLE WalletTransactionData_new RENAME TO WalletTransactionData")
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_WalletTransactionData_invoice ON WalletTransactionData (invoice)",
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_WalletTransactionData_paymentHash " +
                        "ON WalletTransactionData (paymentHash)",
                )

                // Recreate ReceiveRequestData without userId
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ReceiveRequestData_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        walletId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        fulfilledAt INTEGER,
                        payload TEXT NOT NULL,
                        amountInBtc TEXT
                    )
                    """,
                )
                connection.execSQL(
                    """
                    INSERT INTO ReceiveRequestData_new (id, walletId, type, createdAt, fulfilledAt, payload, amountInBtc)
                    SELECT id, walletId, type, createdAt, fulfilledAt, payload, amountInBtc FROM ReceiveRequestData
                    """,
                )
                connection.execSQL("DROP TABLE ReceiveRequestData")
                connection.execSQL("ALTER TABLE ReceiveRequestData_new RENAME TO ReceiveRequestData")
                connection.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "index_ReceiveRequestData_walletId_type_payload " +
                        "ON ReceiveRequestData (walletId, type, payload)",
                )
            }
        }

        fun setEncryption(enableEncryption: Boolean) {
            EncryptableTypeConverters.enableEncryption = enableEncryption
        }
    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "RedundantSuppression")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<WalletDatabase> {
    override fun initialize(): WalletDatabase
}
