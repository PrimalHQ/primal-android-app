package net.primal.shared.data.local.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.driver.NativeSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.core.utils.coroutines.IOSDispatcherProvider
import net.primal.core.utils.files.excludeFromBackup
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDomainMask

typealias LocalDatabaseFactory = IosLocalDatabaseFactory

object IosLocalDatabaseFactory {

    const val DATABASES_DIRECTORY_NAME = "databases"

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
        fallbackToDestructiveMigration: Boolean,
        callback: RoomDatabase.Callback? = null,
        pragmaConfig: LocalDatabasePragmaConfig? = null,
        migrations: List<Migration> = emptyList(),
    ): T {
        val dbFilePath = prepareDatabaseFilePath(databaseName = databaseName)
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
     * Resolves the database file path inside the backup-excluded databases directory, moving any
     * files from the legacy Documents-root location first. The move must complete before Room
     * opens the database; a crash mid-move is safe because the remaining files are moved on the
     * next launch, before any open.
     */
    fun prepareDatabaseFilePath(databaseName: String): String {
        val directoryPath = databasesDirectory()
        moveLegacyDatabaseFiles(databaseName = databaseName, targetDirectory = directoryPath)
        return "$directoryPath/$databaseName"
    }

    /**
     * Deletes obsolete database files by name (each with its `-wal`/`-shm`/`-journal` and `.lck`
     * sidecars) from both the databases directory and the legacy Documents-root location.
     * Missing files are a no-op, so this is safe to call unconditionally on every startup.
     */
    fun deleteDatabases(names: List<String>) {
        names.forEach { deleteDatabaseFiles(it) }
    }

    /**
     * Deletes the database (with sidecars) if its main file has reached [maxSizeBytes]. Must be
     * called before the database is opened; intended for cache-only databases whose data is
     * re-fetchable.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun deleteDatabaseIfOversized(databaseName: String, maxSizeBytes: Long) {
        val path = "${databasesDirectory()}/$databaseName"
        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path = path, error = null)
        val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: return
        if (fileSize >= maxSizeBytes) {
            deleteDatabaseFiles(databaseName)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun databasesDirectory(): String {
        val path = documentDirectory() + "/$DATABASES_DIRECTORY_NAME"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(path)) {
            fileManager.createDirectoryAtPath(
                path = path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        excludeFromBackup(path)
        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun moveLegacyDatabaseFiles(databaseName: String, targetDirectory: String) {
        val fileManager = NSFileManager.defaultManager
        val legacyDirectory = documentDirectory()
        databaseFileNames(databaseName).forEach { fileName ->
            val legacyPath = "$legacyDirectory/$fileName"
            val targetPath = "$targetDirectory/$fileName"
            if (fileManager.fileExistsAtPath(legacyPath) && !fileManager.fileExistsAtPath(targetPath)) {
                fileManager.moveItemAtPath(legacyPath, toPath = targetPath, error = null)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteDatabaseFiles(databaseName: String) {
        val fileManager = NSFileManager.defaultManager
        val directories = listOf(documentDirectory(), databasesDirectory())
        databaseFileNames(databaseName).forEach { fileName ->
            directories.forEach { directory ->
                val path = "$directory/$fileName"
                if (fileManager.fileExistsAtPath(path = path)) {
                    fileManager.removeItemAtPath(path = path, error = null)
                }
            }
        }
    }

    private fun databaseFileNames(databaseName: String) =
        listOf(
            databaseName,
            "$databaseName-wal",
            "$databaseName-shm",
            "$databaseName-journal",
            "$databaseName.lck",
        )

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
