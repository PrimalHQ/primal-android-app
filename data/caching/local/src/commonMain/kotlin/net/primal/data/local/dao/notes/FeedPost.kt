package net.primal.data.local.dao.notes

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventRelayHints
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.polls.PollData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.streams.StreamData

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["postId"],
    )
    val uris: List<EventUri> = emptyList(),

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["postId"],
    )
    val nostrUris: List<EventUriNostr> = emptyList(),

    @Relation(
        entityColumns = ["ownerId"],
        parentColumns = ["authorId"],
    )
    val author: ProfileData? = null,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["postId"],
    )
    val eventStats: EventStats? = null,

    @Embedded
    val userStats: FeedPostUserStats? = null,

    @Relation(
        entityColumns = ["ownerId"],
        parentColumns = ["repostAuthorId"],
    )
    val repostAuthor: ProfileData? = null,

    @Relation(
        entityColumns = ["ownerId"],
        parentColumns = ["replyToAuthorId"],
    )
    val replyToAuthor: ProfileData? = null,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["postId"],
    )
    val eventRelayHints: EventRelayHints? = null,

    @Relation(
        entityColumns = ["eventId"],
        parentColumns = ["postId"],
    )
    val eventZaps: List<EventZap> = emptyList(),

    @Relation(
        entityColumns = ["tagValue"],
        parentColumns = ["postId"],
    )
    val bookmark: PublicBookmark? = null,

    @Relation(
        entityColumns = ["mainHostId"],
        parentColumns = ["authorId"],
    )
    val streams: List<StreamData> = emptyList(),

    @Relation(
        entityColumns = ["postId"],
        parentColumns = ["postId"],
    )
    val pollData: PollData? = null,
)
