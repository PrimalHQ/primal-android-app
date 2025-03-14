package net.primal.data.local.dao.reads

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.notes.PostWithAuthorData
import net.primal.data.local.dao.profiles.ProfileData

data class Highlight(
    @Embedded
    val data: HighlightData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData? = null,

    @Relation(entity = PostData::class, entityColumn = "replyToPostId", parentColumn = "highlightId")
    val comments: List<PostWithAuthorData> = emptyList(),
)
