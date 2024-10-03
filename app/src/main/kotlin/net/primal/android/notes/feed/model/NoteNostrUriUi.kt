package net.primal.android.notes.feed.model

import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.notes.db.ReferencedArticle
import net.primal.android.notes.db.ReferencedNote
import net.primal.android.notes.db.ReferencedUser

data class NoteNostrUriUi(
    val uri: String,
    val referencedNote: ReferencedNote?,
    val referencedArticle: ReferencedArticle?,
    val referencedUser: ReferencedUser?,
)

fun NoteNostrUri.asNoteNostrUriUi() =
    NoteNostrUriUi(
        uri = this.uri,
        referencedNote = this.referencedNote,
        referencedArticle = this.referencedArticle,
        referencedUser = this.referencedUser,
    )
