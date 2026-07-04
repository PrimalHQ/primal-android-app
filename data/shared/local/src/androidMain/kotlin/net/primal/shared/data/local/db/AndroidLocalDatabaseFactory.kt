package net.primal.shared.data.local.db

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import net.primal.core.utils.coroutines.AndroidDispatcherProvider

typealias LocalDatabaseFactory = AndroidLocalDatabaseFactory

object AndroidLocalDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        context: Context,
        databaseName: String,
        fallbackToDestructiveMigration: Boolean,
        callback: RoomDatabase.Callback? = null,
        migrations: List<Migration> = emptyList(),
    ): T {
        val appContext = context.applicationContext
        val dbFile = context.getDatabasePath(databaseName)
        val debugDiagnostics = (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return buildLocalDatabase(
            fallbackToDestructiveMigration = fallbackToDestructiveMigration,
            logEngineDiagnostics = debugDiagnostics,
            migrations = migrations,
        ) {
            Room.databaseBuilder<T>(context = appContext, name = dbFile.absolutePath)
                .setQueryCoroutineContext(AndroidDispatcherProvider().io())
                .setDriver(BundledSQLiteDriver())
                .run { if (callback != null) addCallback(callback) else this }
        }
    }
}
