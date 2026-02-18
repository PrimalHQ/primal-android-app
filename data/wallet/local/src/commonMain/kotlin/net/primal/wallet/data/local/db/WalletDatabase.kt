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
import net.primal.wallet.data.local.dao.WalletDao
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.dao.WalletSettings
import net.primal.wallet.data.local.dao.WalletSettingsDao
import net.primal.wallet.data.local.dao.WalletTransactionDao
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.dao.WalletTransactionRemoteKey
import net.primal.wallet.data.local.dao.WalletTransactionRemoteKeyDao
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
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 3, to = 4),
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
