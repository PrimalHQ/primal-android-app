package net.primal.android.notes.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.bookmarks.db.PublicBookmark
import net.primal.android.nostr.db.EventRelayHints
import net.primal.android.profile.db.ProfileData
import net.primal.android.events.db.EventStats
import net.primal.android.events.db.EventZap

data class FeedPost(

    @Embedded
    val data: FeedPostData,

    @Relation(
        entityColumn = "eventId",
        parentColumn = "postId",
    )
    val attachments: List<NoteAttachment>,

    @Relation(
        entityColumn = "noteId",
        parentColumn = "postId",
    )
    val nostrUris: List<NoteNostrUri>,

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
