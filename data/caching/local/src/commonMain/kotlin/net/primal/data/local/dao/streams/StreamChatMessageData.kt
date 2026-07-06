package net.primal.data.local.dao.streams

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
