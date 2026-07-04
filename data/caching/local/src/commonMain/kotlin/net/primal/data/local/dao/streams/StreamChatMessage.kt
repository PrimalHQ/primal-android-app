package net.primal.data.local.dao.streams

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.profiles.ProfileData

data class StreamChatMessage(
    @Embedded
    val data: StreamChatMessageData,

    @Relation(
        parentColumns = ["authorId"],
        entityColumns = ["ownerId"],
    )
    val author: ProfileData? = null,
)
