package net.primal.wallet.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import net.primal.wallet.data.local.dao.WalletTransactionDao
import net.primal.wallet.data.local.dao.WalletTransactionData

@Database(
    entities = [
        WalletTransactionData::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletTransactions(): WalletTransactionDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "RedundantSuppression")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<WalletDatabase> {
    override fun initialize(): WalletDatabase
}
