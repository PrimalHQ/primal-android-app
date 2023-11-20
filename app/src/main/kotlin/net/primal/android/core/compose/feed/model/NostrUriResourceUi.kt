package net.primal.android.core.compose.feed.model

import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.feed.db.ReferencedPost
import net.primal.android.feed.db.ReferencedUser

data class NostrUriResourceUi(
    val uri: String,
    val referencedUser: ReferencedUser?,
    val referencedPost: ReferencedPost?,
)

fun NoteNostrUri.asNostrUriResourceUi() =
    NostrUriResourceUi(
        uri = this.uri,
        referencedPost = this.referencedPost,
        referencedUser = this.referencedUser,
    )
