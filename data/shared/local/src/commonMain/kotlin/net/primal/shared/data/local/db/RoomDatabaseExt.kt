package net.primal.shared.data.local.db

import androidx.room3.RoomDatabase
import androidx.room3.deferredTransaction
import androidx.room3.executeSQL
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection

suspend fun <T> RoomDatabase.withTransaction(block: suspend () -> T): T {
    return this.useWriterConnection { writeConnection ->
        writeConnection.deferredTransaction {
            block()
        }
    }
}

/**
 * Runs a truncating write-ahead log checkpoint (`PRAGMA wal_checkpoint(TRUNCATE)`), flushing the WAL
 * into the database file and shrinking the `-wal` file back to zero.
 *
 * Truncation only completes while no reader holds an open WAL snapshot, so invoke this at an idle
 * point, such as when the application moves to the background.
 */
suspend fun RoomDatabase.walCheckpointTruncate() {
    this.useWriterConnection { transactor ->
        transactor.executeSQL("PRAGMA wal_checkpoint(TRUNCATE)")
    }
}

suspend fun <T> RoomDatabase.withReadOnlyTransaction(block: suspend () -> T): T {
    return this.useReaderConnection { readerConnection ->
        readerConnection.deferredTransaction {
            block()
        }
    }
}
