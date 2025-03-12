package net.primal.android.notes.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.bookmarks.db.PublicBookmark
import net.primal.android.events.db.EventStats
import net.primal.android.events.db.EventUri
import net.primal.android.events.db.EventUriNostr
import net.primal.android.events.db.EventZap
import net.primal.android.nostr.db.EventRelayHints
import net.primal.android.profile.db.ProfileData

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val uris: List<EventUri>,

    @Relation(
        entityColumn = "noteId",
        parentColumn = "postId",
    )
    val nostrUris: List<EventUriNostr>,

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
