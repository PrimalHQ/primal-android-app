package net.primal.android.messages.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.feed.db.MediaResource
import net.primal.android.profile.db.ProfileData

data class MessageConversation(

    @Embedded
    val data: MessageConversationData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "participantId",
    )
    val participant: ProfileData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "participantMetadataId",
    )
    val participantResources: List<MediaResource> = emptyList(),

    @Relation(
        entityColumn = "messageId",
        parentColumn = "lastMessageId",
    )
    val lastMessage: MessageData,

)
