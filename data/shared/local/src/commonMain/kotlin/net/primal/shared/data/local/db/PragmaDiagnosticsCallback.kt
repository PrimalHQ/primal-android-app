package net.primal.shared.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import io.github.aakira.napier.Napier

/**
 * Debug-only Room callback that logs the live SQLite engine settings for every connection Room opens
 * — on every target (Android / iOS / desktop) and every database routed through [buildLocalDatabase]
 * (caching / wallet / account). It is attached only when diagnostics are enabled (see
 * `logEngineDiagnostics` on [buildLocalDatabase]); it is never attached in release builds.
 *
 * Use it to confirm what the driver actually resolves PRAGMAs to on-device — journal mode,
 * synchronous level, busy timeout, cache / mmap sizing, page size — and to verify PRAGMA-tuning
 * changes after they land.
 *
 * Reading method: run a debug build, exercise each screen (scroll the feed to force the reader pool
 * to open), then in logcat:
 *   adb logcat | grep DbPragma
 * Count distinct `conn=` values per `db=` to get that database's connection-pool size.
 */
internal object PragmaDiagnosticsCallback : RoomDatabase.Callback() {

    override fun onOpen(connection: SQLiteConnection) {
        val dbFile = connection.readPragma(pragma = "database_list", columnIndex = 2)
        Napier.i(tag = "DbPragma") {
            "onOpen conn=${connection.hashCode().toString(radix = 16)} db=$dbFile " +
                "journal_mode=${connection.readPragma("journal_mode")} " +
                "synchronous=${connection.readPragma("synchronous")} " +
                "busy_timeout=${connection.readPragma("busy_timeout")} " +
                "cache_size=${connection.readPragma("cache_size")} " +
                "mmap_size=${connection.readPragma("mmap_size")} " +
                "temp_store=${connection.readPragma("temp_store")} " +
                "page_size=${connection.readPragma("page_size")}"
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun SQLiteConnection.readPragma(pragma: String, columnIndex: Int = 0): String {
        val statement = prepare("PRAGMA $pragma")
        return try {
            if (statement.step()) statement.getText(columnIndex) else "<none>"
        } catch (error: Throwable) {
            "<err:${error.message}>"
        } finally {
            statement.close()
        }
    }
}
