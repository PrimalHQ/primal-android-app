package net.primal.data.local.dao.streams

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StreamChatMessageData(
    @PrimaryKey
    val messageId: String,

    @ColumnInfo(index = true)
    val streamATag: String,

    @ColumnInfo(index = true)
    val authorId: String,

    val createdAt: Long,
    val content: String,
    val raw: String,
    val client: String?,
)
