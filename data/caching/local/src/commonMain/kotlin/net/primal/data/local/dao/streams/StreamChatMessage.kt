package net.primal.data.local.dao.streams

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class StreamChatMessage(
    @Embedded
    val data: StreamChatMessageData,

    @Relation(
        parentColumn = "authorId",
        entityColumn = "ownerId",
    )
    val author: ProfileData? = null,
)
