package net.primal.android.notes.feed.model

import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.domain.NostrUriType
import net.primal.android.notes.db.ReferencedArticle
import net.primal.android.notes.db.ReferencedHighlight
import net.primal.android.notes.db.ReferencedNote
import net.primal.android.notes.db.ReferencedUser
import net.primal.android.notes.db.ReferencedZap

data class NoteNostrUriUi(
    val uri: String,
    val type: NostrUriType,
    val referencedEventAlt: String?,
    val referencedHighlight: ReferencedHighlight?,
    val referencedNote: ReferencedNote?,
    val referencedArticle: ReferencedArticle?,
    val referencedUser: ReferencedUser?,
    val referencedZap: ReferencedZap?,
    val position: Int,
)

fun NoteNostrUri.asNoteNostrUriUi() =
    NoteNostrUriUi(
        uri = this.uri,
        type = this.type,
        referencedEventAlt = this.referencedEventAlt,
        referencedHighlight = this.referencedHighlight,
        referencedNote = this.referencedNote,
        referencedArticle = this.referencedArticle,
        referencedUser = this.referencedUser,
        referencedZap = referencedZap,
        position = this.position,
    )
