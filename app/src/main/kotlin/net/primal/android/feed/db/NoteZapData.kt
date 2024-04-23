package net.primal.android.feed.db

import androidx.room.Entity

@Entity(
    primaryKeys = [
        "zapSenderId",
        "noteId",
        "zappedAt",
    ],
)
data class NoteZapData(
    val zapSenderId: String,
    val zapReceiverId: String,
    val noteId: String,
    val zappedAt: Long,
    val amountInMillisats: String,
    val message: String,
)
