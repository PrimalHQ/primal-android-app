package net.primal.shared.data.local.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import net.primal.core.utils.coroutines.JvmDispatcherProvider

typealias LocalDatabaseFactory = JvmLocalDatabaseFactory

object JvmLocalDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
        pragmaConfig: LocalDatabasePragmaConfig? = null,
        migrations: List<Migration> = emptyList(),
    ): T {
        return buildLocalDatabase(
            fallbackToDestructiveMigration = false,
            pragmaConfig = pragmaConfig,
            migrations = migrations,
        ) {
            val dbFile = File(System.getProperty("java.io.tmpdir"), databaseName)
            Room.databaseBuilder<T>(name = dbFile.absolutePath)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(JvmDispatcherProvider().io())
        }
    }

    /**
     * Deletes obsolete database files by name (each with its `-wal`/`-shm`/`-journal` sidecars)
     * (each with its `-wal`/`-shm`/`-journal` and `.lck` sidecars) from the tmp directory.
     * Missing files are a no-op, so this is safe to call unconditionally.
     */
    fun deleteDatabases(names: List<String>) {
        val tmpDir = System.getProperty("java.io.tmpdir")
        names.forEach { name ->
            listOf(name, "$name-wal", "$name-shm", "$name-journal", "$name.lck").forEach { fileName ->
                File(tmpDir, fileName).delete()
            }
        }
    }
}
