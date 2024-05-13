package net.primal.android.note.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteZapData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val zapSenderId: String,
    val zapReceiverId: String,
    val noteId: String,
    val zapRequestAt: Long,
    val zapReceiptAt: Long,
    val amountInBtc: Double,
    val message: String?,
)
