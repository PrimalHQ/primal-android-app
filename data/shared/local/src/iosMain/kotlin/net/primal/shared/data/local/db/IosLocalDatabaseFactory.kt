package net.primal.shared.data.local.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.driver.NativeSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.core.utils.coroutines.IOSDispatcherProvider
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

typealias LocalDatabaseFactory = IosLocalDatabaseFactory

object IosLocalDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
        fallbackToDestructiveMigration: Boolean,
        callback: RoomDatabase.Callback? = null,
        pragmaConfig: LocalDatabasePragmaConfig? = null,
        migrations: List<Migration> = emptyList(),
    ): T {
        val dbFilePath = documentDirectory() + "/$databaseName"
        return buildLocalDatabase(
            fallbackToDestructiveMigration = fallbackToDestructiveMigration,
            pragmaConfig = pragmaConfig,
            migrations = migrations,
        ) {
            Room.databaseBuilder<T>(name = dbFilePath)
                .setQueryCoroutineContext(IOSDispatcherProvider().io())
                .setDriver(NativeSQLiteDriver())
                .run {
                    if (callback != null) {
                        this.addCallback(callback)
                    } else {
                        this
                    }
                }
        }
    }

    /**
     * Deletes obsolete database files by name (each with its `-wal`/`-shm`/`-journal` and `.lck`
     * sidecars) from the Documents directory. Missing files are a no-op, so this is safe to call
     * unconditionally on every startup.
     */
    fun deleteDatabases(names: List<String>) {
        names.forEach { deleteDatabaseFiles(it) }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteDatabaseFiles(databaseName: String) {
        val basePath = documentDirectory() + "/$databaseName"
        val fileManager = NSFileManager.defaultManager
        listOf(
            basePath,
            "$basePath-wal",
            "$basePath-shm",
            "$basePath-journal",
            "$basePath.lck",
        ).forEach { path ->
            if (fileManager.fileExistsAtPath(path = path)) {
                fileManager.removeItemAtPath(path = path, error = null)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }
}
