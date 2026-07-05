package net.primal.shared.data.local.db

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * [RoomDatabase.Callback] that applies a [LocalDatabasePragmaConfig] to every connection the database
 * opens, including reader connections. Attached by [buildLocalDatabase] when a configuration is
 * supplied. See [PragmaDiagnosticsCallback] to inspect the resulting values on device.
 */
internal class PragmaConfigCallback(
    private val config: LocalDatabasePragmaConfig,
) : RoomDatabase.Callback() {

    override suspend fun onOpen(connection: SQLiteConnection) {
        config.journalSizeLimitBytes?.let { connection.execSQL("PRAGMA journal_size_limit = $it") }
        config.cacheSizeKib?.let { connection.execSQL("PRAGMA cache_size = ${-it}") }
        config.mmapSizeBytes?.let { connection.execSQL("PRAGMA mmap_size = $it") }
        if (config.tempStoreMemory) connection.execSQL("PRAGMA temp_store = MEMORY")
    }
}
