package net.primal.android.messages.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.feed.db.MediaResource
import net.primal.android.feed.db.NostrResource

data class DirectMessage(

    @Embedded
    val data: DirectMessageData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "messageId",
    )
    val mediaResources: List<MediaResource>,

    @Relation(
        entityColumn = "postId",
        parentColumn = "messageId",
    )
    val nostrUris: List<NostrResource>,
)
