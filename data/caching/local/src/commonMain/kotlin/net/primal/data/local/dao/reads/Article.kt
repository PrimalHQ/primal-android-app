package net.primal.data.local.dao.reads

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData

data class Article(
    @Embedded
    val data: ArticleData,

    @Relation(entityColumns = ["ownerId"], parentColumns = ["authorId"])
    val author: ProfileData? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["eventId"])
    val eventStats: EventStats? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["eventId"])
    val userEventStats: EventUserStats? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["aTag"])
    val eventZaps: List<EventZap> = emptyList(),

    @Relation(entityColumns = ["tagValue"], parentColumns = ["aTag"])
    val bookmark: PublicBookmark? = null,

    @Relation(entity = HighlightData::class, entityColumns = ["referencedEventATag"], parentColumns = ["aTag"])
    val highlights: List<Highlight> = emptyList(),
)
