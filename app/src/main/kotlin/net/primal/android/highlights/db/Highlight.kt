package net.primal.android.highlights.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.notes.db.PostData
import net.primal.android.notes.db.PostWithAuthorData
import net.primal.android.profile.db.ProfileData

data class Highlight(
    @Embedded
    val data: HighlightData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData? = null,

    @Relation(entity = PostData::class, entityColumn = "replyToPostId", parentColumn = "highlightId")
    val comments: List<PostWithAuthorData> = emptyList(),
)
