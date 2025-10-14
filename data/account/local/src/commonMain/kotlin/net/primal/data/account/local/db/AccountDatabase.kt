package net.primal.data.account.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import net.primal.data.account.local.dao.ConnectionData
import net.primal.data.account.local.dao.ConnectionDataDao
import net.primal.data.account.local.dao.PermissionData
import net.primal.data.account.local.dao.PermissionDataDao
import net.primal.shared.data.local.serialization.EncryptableTypeConverters
import net.primal.shared.data.local.serialization.ListsTypeConverters

@Database(
    entities = [
        ConnectionData::class,
        PermissionData::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ListsTypeConverters::class, EncryptableTypeConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun connections(): ConnectionDataDao
    abstract fun permissions(): PermissionDataDao

    companion object {
        fun setEncryption(enableEncryption: Boolean) {
            EncryptableTypeConverters.enableEncryption = enableEncryption
        }
    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "RedundantSuppression")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AccountDatabase> {
    override fun initialize(): AccountDatabase
}
