package net.primal.android.articles.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.bookmarks.db.PublicBookmark
import net.primal.android.note.db.EventStats
import net.primal.android.note.db.EventUserStats
import net.primal.android.note.db.EventZap
import net.primal.android.profile.db.ProfileData

data class Article(
    @Embedded
    val data: ArticleData,

    @Relation(entityColumn = "ownerId", parentColumn = "authorId")
    val author: ProfileData? = null,

    @Relation(entityColumn = "eventId", parentColumn = "eventId")
    val eventStats: EventStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "eventId")
    val userEventStats: EventUserStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "eventId")
    val eventZaps: List<EventZap> = emptyList(),

    @Relation(entityColumn = "tagValue", parentColumn = "aTag")
    val bookmark: PublicBookmark? = null,
)
