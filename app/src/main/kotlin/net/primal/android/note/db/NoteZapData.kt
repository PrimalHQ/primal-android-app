package net.primal.android.note.db

import androidx.room.Entity

@Entity(
    primaryKeys = [
        "zapSenderId",
        "noteId",
        "zapRequestAt",
    ],
)
data class NoteZapData(
    val zapSenderId: String,
    val zapReceiverId: String,
    val noteId: String,
    val zapRequestAt: Long,
    val zapReceiptAt: Long,
    val amountInBtc: Double,
    val message: String?,
)
