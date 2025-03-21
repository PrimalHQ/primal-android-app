package net.primal.data.local.dao.notes

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventRelayHints
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val uris: List<EventUri> = emptyList(),

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val nostrUris: List<EventUriNostr> = emptyList(),

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "authorId",
    )
    val author: ProfileData? = null,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val eventStats: EventStats? = null,

    @Embedded
    val userStats: FeedPostUserStats? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "repostAuthorId",
    )
    val repostAuthor: ProfileData? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "replyToAuthorId",
    )
    val replyToAuthor: ProfileData? = null,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val eventRelayHints: EventRelayHints? = null,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val eventZaps: List<EventZap> = emptyList(),

    @Relation(
        entityColumn = "tagValue",
        parentColumn = "postId",
    )
    val bookmark: PublicBookmark? = null,
)
