package net.primal.data.local.dao.reads

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData

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

    @Relation(entity = HighlightData::class, entityColumn = "referencedEventATag", parentColumn = "aTag")
    val highlights: List<Highlight> = emptyList(),
)
