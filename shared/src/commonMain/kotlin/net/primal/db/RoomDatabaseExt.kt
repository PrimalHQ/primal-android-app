package net.primal.db

import androidx.room.RoomDatabase
import androidx.room.deferredTransaction
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection

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
