package net.primal.android.articles.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.bookmarks.db.PublicBookmark
import net.primal.android.highlights.db.HighlightData
import net.primal.android.profile.db.ProfileData
import net.primal.android.stats.db.EventStats
import net.primal.android.stats.db.EventUserStats
import net.primal.android.stats.db.EventZap

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

    @Relation(entityColumn = "referencedEventId", parentColumn = "aTag")
    val highlights: List<HighlightData> = emptyList(),
)
