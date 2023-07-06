package net.primal.android.profile.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.feed.db.MediaResource

data class Profile(

    @Embedded
    val metadata: ProfileMetadata? = null,

    @Relation(
        entityColumn = "profileId",
        parentColumn = "ownerId",
    )
    val stats: ProfileStats? = null,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "eventId",
    )
    val resources: List<MediaResource> = emptyList(),
)
