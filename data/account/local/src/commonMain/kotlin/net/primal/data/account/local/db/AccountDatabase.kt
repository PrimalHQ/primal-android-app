package net.primal.data.account.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import net.primal.data.account.local.dao.AppConnectionData
import net.primal.data.account.local.dao.AppConnectionDataDao
import net.primal.data.account.local.dao.AppPermissionData
import net.primal.data.account.local.dao.AppPermissionDataDao
import net.primal.data.account.local.dao.AppSessionData
import net.primal.data.account.local.dao.AppSessionDataDao
import net.primal.shared.data.local.serialization.EncryptableTypeConverters
import net.primal.shared.data.local.serialization.ListsTypeConverters

@Database(
    entities = [
        AppConnectionData::class,
        AppPermissionData::class,
        AppSessionData::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(ListsTypeConverters::class, EncryptableTypeConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun connections(): AppConnectionDataDao
    abstract fun permissions(): AppPermissionDataDao
    abstract fun sessions(): AppSessionDataDao

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
