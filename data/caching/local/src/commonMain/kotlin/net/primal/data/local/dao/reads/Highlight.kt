package net.primal.data.local.dao.reads

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.notes.PostWithAuthorData
import net.primal.data.local.dao.profiles.ProfileData

data class Highlight(
    @Embedded
    val data: HighlightData,

    @Relation(entityColumns = ["ownerId"], parentColumns = ["authorId"])
    val author: ProfileData? = null,

    @Relation(entity = PostData::class, entityColumns = ["replyToPostId"], parentColumns = ["highlightId"])
    val comments: List<PostWithAuthorData> = emptyList(),
)
