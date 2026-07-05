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
        pragmaConfig: LocalDatabasePragmaConfig? = null,
        migrations: List<Migration> = emptyList(),
    ): T {
        val appContext = context.applicationContext
        val dbFile = context.getDatabasePath(databaseName)
        val debugDiagnostics = (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return buildLocalDatabase(
            fallbackToDestructiveMigration = fallbackToDestructiveMigration,
            logEngineDiagnostics = debugDiagnostics,
            pragmaConfig = pragmaConfig,
            migrations = migrations,
        ) {
            Room.databaseBuilder<T>(context = appContext, name = dbFile.absolutePath)
                .setQueryCoroutineContext(AndroidDispatcherProvider().io())
                .setDriver(BundledSQLiteDriver())
                .run { if (callback != null) addCallback(callback) else this }
        }
    }

    /**
     * Deletes obsolete database files by name (each with its `-wal`/`-shm`/`-journal` and
     * `.lck` sidecars). Missing files are a no-op, so this is safe to call unconditionally on
     * every startup.
     */
    fun deleteDatabases(context: Context, names: List<String>) {
        val appContext = context.applicationContext
        names.forEach { name ->
            // Removes the db plus its -journal/-wal/-shm/-mj sidecars.
            appContext.deleteDatabase(name)
            // BundledSQLiteDriver keeps a "<name>.lck" lock file that deleteDatabase() misses.
            appContext.getDatabasePath("$name.lck").delete()
        }
    }
}
