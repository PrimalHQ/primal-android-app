package net.primal.data.local.dao.reads

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.notes.FeedAuthorLite
import net.primal.data.local.dao.profiles.ProfileData

/**
 * Feed projection of [Article] — no `highlights` relation (feed cards never render highlights,
 * and the relation drags HighlightData, PostData and their author profiles into every feed page
 * load) and a [FeedAuthorLite] author instead of the full [ProfileData] row.
 */
data class ArticleFeedItem(
    @Embedded
    val data: ArticleFeedData,

    @Relation(entity = ProfileData::class, entityColumns = ["ownerId"], parentColumns = ["authorId"])
    val author: FeedAuthorLite? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["eventId"])
    val eventStats: EventStats? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["eventId"])
    val userEventStats: EventUserStats? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["aTag"])
    val eventZaps: List<EventZap> = emptyList(),

    @Relation(entityColumns = ["tagValue"], parentColumns = ["aTag"])
    val bookmark: PublicBookmark? = null,
)
