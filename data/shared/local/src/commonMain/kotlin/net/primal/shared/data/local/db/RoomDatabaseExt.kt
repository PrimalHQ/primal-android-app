package net.primal.shared.data.local.db

import androidx.room3.RoomDatabase
import androidx.room3.deferredTransaction
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection

suspend fun <T> RoomDatabase.withTransaction(block: suspend () -> T): T {
    return this.useWriterConnection { writeConnection ->
        writeConnection.deferredTransaction {
            block()
        }
    }
}

suspend fun <T> RoomDatabase.withReadOnlyTransaction(block: suspend () -> T): T {
    return this.useReaderConnection { readerConnection ->
        readerConnection.deferredTransaction {
            block()
        }
    }
}
