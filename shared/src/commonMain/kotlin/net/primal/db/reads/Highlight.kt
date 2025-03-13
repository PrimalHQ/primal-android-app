package net.primal.db.reads

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.db.notes.PostData
import net.primal.db.notes.PostWithAuthorData
import net.primal.db.profiles.ProfileData

data class Highlight(
    @Embedded
    val data: HighlightData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData? = null,

    @Relation(entity = PostData::class, entityColumn = "replyToPostId", parentColumn = "highlightId")
    val comments: List<PostWithAuthorData> = emptyList(),
)
