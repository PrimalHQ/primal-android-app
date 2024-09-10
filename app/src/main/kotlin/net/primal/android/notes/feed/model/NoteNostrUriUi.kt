package net.primal.android.notes.feed.model

import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.notes.db.ReferencedPost
import net.primal.android.notes.db.ReferencedUser

data class NoteNostrUriUi(
    val uri: String,
    val referencedUser: ReferencedUser?,
    val referencedPost: ReferencedPost?,
)

fun NoteNostrUri.asNoteNostrUriUi() =
    NoteNostrUriUi(
        uri = this.uri,
        referencedPost = this.referencedPost,
        referencedUser = this.referencedUser,
    )
