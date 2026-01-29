package net.primal.wallet.data.local.db

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
import net.primal.wallet.data.local.dao.SparkWalletData
import net.primal.wallet.data.local.dao.WalletDao
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.dao.WalletSettings
import net.primal.wallet.data.local.dao.WalletSettingsDao
import net.primal.wallet.data.local.dao.WalletTransactionDao
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.dao.nwc.NwcConnectionDao
import net.primal.wallet.data.local.dao.nwc.NwcConnectionData
import net.primal.wallet.data.local.dao.nwc.NwcDailyBudgetData
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldDao
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldData

@Database(
    entities = [
        WalletInfo::class,
        NostrWalletData::class,
        PrimalWalletData::class,
        SparkWalletData::class,
        ActiveWalletData::class,
        WalletTransactionData::class,
        WalletSettings::class,
        NwcConnectionData::class,
        NwcPaymentHoldData::class,
        NwcDailyBudgetData::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(ListsTypeConverters::class, EncryptableTypeConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun wallet(): WalletDao
    abstract fun walletTransactions(): WalletTransactionDao
    abstract fun walletSettings(): WalletSettingsDao
    abstract fun nwcConnections(): NwcConnectionDao
    abstract fun nwcPaymentHolds(): NwcPaymentHoldDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                // 1. Create new tables for Spark and NWC support
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS SparkWalletData (
                        walletId TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        seedWords TEXT NOT NULL,
                        backedUp INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_SparkWalletData_userId ON SparkWalletData (userId)",
                )

                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS NwcConnectionData (
                        secretPubKey TEXT NOT NULL PRIMARY KEY,
                        walletId TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        servicePubKey TEXT NOT NULL,
                        servicePrivateKey TEXT NOT NULL,
                        relay TEXT NOT NULL,
                        appName TEXT NOT NULL,
                        dailyBudgetSats TEXT
                    )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS NwcPaymentHoldData (
                        holdId TEXT NOT NULL PRIMARY KEY,
                        connectionId TEXT NOT NULL,
                        requestId TEXT NOT NULL,
                        amountSats TEXT NOT NULL,
                        status TEXT NOT NULL,
                        budgetDate TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        expiresAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_NwcPaymentHoldData_connectionId
                        ON NwcPaymentHoldData (connectionId)
                    """.trimIndent(),
                )
                connection.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_NwcPaymentHoldData_budgetDate
                        ON NwcPaymentHoldData (budgetDate)
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS NwcDailyBudgetData (
                        connectionId TEXT NOT NULL,
                        budgetDate TEXT NOT NULL,
                        confirmedSpendSats TEXT NOT NULL,
                        lastUpdatedAt INTEGER NOT NULL,
                        PRIMARY KEY (connectionId, budgetDate)
                    )
                    """.trimIndent(),
                )

                // 2. Add new columns to WalletTransactionData
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN txKind TEXT NOT NULL DEFAULT 'LIGHTNING'",
                )
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN onChainAddress TEXT",
                )
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN onChainTxId TEXT",
                )
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN preimage TEXT",
                )
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN paymentHash TEXT",
                )
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN amountInUsd TEXT",
                )
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN exchangeRate TEXT",
                )
                connection.execSQL(
                    "ALTER TABLE WalletTransactionData ADD COLUMN otherLightningAddress TEXT",
                )

                // 3. Migrate NWC transaction data (preserve preimage/paymentHash)
                connection.execSQL(
                    """
                    UPDATE WalletTransactionData
                    SET preimage = (
                        SELECT preimage FROM NostrTransactionData
                        WHERE NostrTransactionData.transactionId = WalletTransactionData.transactionId
                    ),
                    paymentHash = (
                        SELECT paymentHash FROM NostrTransactionData
                        WHERE NostrTransactionData.transactionId = WalletTransactionData.transactionId
                    )
                    WHERE transactionId IN (SELECT transactionId FROM NostrTransactionData)
                    """.trimIndent(),
                )

                // 4. Populate txKind based on existing data
                connection.execSQL(
                    """
                    UPDATE WalletTransactionData SET txKind =
                        CASE
                            WHEN walletType = 'PRIMAL' AND transactionId IN (
                                SELECT transactionId FROM PrimalTransactionData WHERE isStorePurchase = 1
                            ) THEN 'STORE_PURCHASE'
                            WHEN walletType = 'PRIMAL' AND transactionId IN (
                                SELECT transactionId FROM PrimalTransactionData WHERE isZap = 1
                            ) THEN 'ZAP'
                            WHEN walletType = 'PRIMAL' AND transactionId IN (
                                SELECT transactionId FROM PrimalTransactionData WHERE onChainAddress IS NOT NULL
                            ) THEN 'ON_CHAIN'
                            WHEN zappedEntity IS NOT NULL THEN 'ZAP'
                            ELSE 'LIGHTNING'
                        END
                    """.trimIndent(),
                )

                // 5. Drop NostrTransactionData table
                connection.execSQL("DROP TABLE IF EXISTS NostrTransactionData")

                // 6. Migrate data from PrimalTransactionData to WalletTransactionData
                connection.execSQL(
                    """
                    UPDATE WalletTransactionData
                    SET amountInUsd = (
                        SELECT amountInUsd FROM PrimalTransactionData
                        WHERE PrimalTransactionData.transactionId = WalletTransactionData.transactionId
                    ),
                    exchangeRate = (
                        SELECT exchangeRate FROM PrimalTransactionData
                        WHERE PrimalTransactionData.transactionId = WalletTransactionData.transactionId
                    ),
                    otherLightningAddress = (
                        SELECT otherLightningAddress FROM PrimalTransactionData
                        WHERE PrimalTransactionData.transactionId = WalletTransactionData.transactionId
                    ),
                    onChainAddress = COALESCE(onChainAddress, (
                        SELECT onChainAddress FROM PrimalTransactionData
                        WHERE PrimalTransactionData.transactionId = WalletTransactionData.transactionId
                    )),
                    onChainTxId = COALESCE(onChainTxId, (
                        SELECT onChainTxId FROM PrimalTransactionData
                        WHERE PrimalTransactionData.transactionId = WalletTransactionData.transactionId
                    ))
                    WHERE transactionId IN (SELECT transactionId FROM PrimalTransactionData)
                    """.trimIndent(),
                )

                // 7. Drop PrimalTransactionData table
                connection.execSQL("DROP TABLE IF EXISTS PrimalTransactionData")
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
