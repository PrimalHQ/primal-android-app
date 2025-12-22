package net.primal.data.account.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import io.github.aakira.napier.Napier
import net.primal.data.account.local.dao.apps.AppPermissionData
import net.primal.data.account.local.dao.apps.AppPermissionDataDao
import net.primal.data.account.local.dao.apps.local.LocalAppDao
import net.primal.data.account.local.dao.apps.local.LocalAppData
import net.primal.data.account.local.dao.apps.local.LocalAppSessionData
import net.primal.data.account.local.dao.apps.local.LocalAppSessionEventData
import net.primal.data.account.local.dao.apps.remote.RemoteAppConnectionData
import net.primal.data.account.local.dao.apps.remote.RemoteAppConnectionDataDao
import net.primal.data.account.local.dao.apps.remote.RemoteAppPendingNostrEvent
import net.primal.data.account.local.dao.apps.remote.RemoteAppPendingNostrEventDao
import net.primal.data.account.local.dao.apps.remote.RemoteAppSessionData
import net.primal.data.account.local.dao.apps.remote.RemoteAppSessionDataDao
import net.primal.data.account.local.dao.apps.remote.RemoteAppSessionEventData
import net.primal.data.account.local.dao.apps.remote.RemoteAppSessionEventDataDao
import net.primal.shared.data.local.serialization.EncryptableTypeConverters
import net.primal.shared.data.local.serialization.ListsTypeConverters

@Database(
    entities = [
        AppPermissionData::class,
        RemoteAppConnectionData::class,
        RemoteAppSessionData::class,
        RemoteAppSessionEventData::class,
        RemoteAppPendingNostrEvent::class,
        LocalAppData::class,
        LocalAppSessionData::class,
        LocalAppSessionEventData::class,
    ],
    version = 11,
    exportSchema = true,
)
@TypeConverters(
    ListsTypeConverters::class,
    EncryptableTypeConverters::class,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun appPermissions(): AppPermissionDataDao
    abstract fun remoteAppConnections(): RemoteAppConnectionDataDao
    abstract fun remoteAppSessions(): RemoteAppSessionDataDao
    abstract fun remoteAppSessionEvents(): RemoteAppSessionEventDataDao
    abstract fun remoteAppPendingNostrEvents(): RemoteAppPendingNostrEventDao
    abstract fun localApps(): LocalAppDao

    companion object {
        fun provideDatabaseCallback() =
            object : Callback() {
                override fun onOpen(connection: SQLiteConnection) {
                    runCatching {
                        connection.execSQL(
                            """
                                UPDATE RemoteAppSessionData
                                    SET endedAt = strftime('%s', 'now'), activeRelayCount = 0
                                    WHERE endedAt IS NULL
                            """.trimIndent(),
                        )
                        connection.execSQL(
                            """
                                UPDATE LocalAppSessionData
                                    SET endedAt = strftime('%s', 'now')
                                    WHERE endedAt IS NULL
                            """.trimIndent(),
                        )
                        connection.execSQL(
                            """
                                UPDATE RemoteAppSessionEventData
                                    SET completedAt = strftime('%s', 'now'), requestState = 'Rejected'
                                    WHERE requestState = 'PendingUserAction'
                            """.trimIndent(),
                        )
                        connection.execSQL("DELETE FROM RemoteAppPendingNostrEvent")
                    }.onFailure {
                        Napier.d(throwable = it) { "Failed to run onOpen sequence with AccountDatabase" }
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
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AccountDatabase> {
    override fun initialize(): AccountDatabase
}
