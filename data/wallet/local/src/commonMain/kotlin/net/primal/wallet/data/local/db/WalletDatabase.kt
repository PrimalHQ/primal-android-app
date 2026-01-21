package net.primal.wallet.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import net.primal.shared.data.local.serialization.EncryptableTypeConverters
import net.primal.shared.data.local.serialization.ListsTypeConverters
import net.primal.wallet.data.local.dao.ActiveWalletData
import net.primal.wallet.data.local.dao.NostrTransactionData
import net.primal.wallet.data.local.dao.NostrWalletData
import net.primal.wallet.data.local.dao.PrimalTransactionData
import net.primal.wallet.data.local.dao.PrimalWalletData
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
        ActiveWalletData::class,
        WalletTransactionData::class,
        PrimalTransactionData::class,
        NostrTransactionData::class,
        WalletSettings::class,
        NwcConnectionData::class,
        NwcPaymentHoldData::class,
        NwcDailyBudgetData::class,
    ],
    version = 3,
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
