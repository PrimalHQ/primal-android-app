package net.primal.data.local.dao.streams

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["streamATag", "createdAt"]),
    ],
)
data class StreamChatMessageData(
    @PrimaryKey
    val messageId: String,

    val streamATag: String,

    @ColumnInfo(index = true)
    val authorId: String,

    val createdAt: Long,
    val content: String,
    val raw: String,
    val client: String?,
)
