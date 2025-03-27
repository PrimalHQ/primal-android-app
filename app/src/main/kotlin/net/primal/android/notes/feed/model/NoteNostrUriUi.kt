package net.primal.android.notes.feed.model

import net.primal.android.events.db.EventUriNostr
import net.primal.domain.EventUriNostrReference
import net.primal.domain.EventUriNostrType
import net.primal.domain.ReferencedArticle
import net.primal.domain.ReferencedHighlight
import net.primal.domain.ReferencedNote
import net.primal.domain.ReferencedUser
import net.primal.domain.ReferencedZap

data class NoteNostrUriUi(
    val uri: String,
    val type: EventUriNostrType,
    val referencedEventAlt: String?,
    val referencedHighlight: ReferencedHighlight?,
    val referencedNote: ReferencedNote?,
    val referencedArticle: ReferencedArticle?,
    val referencedUser: ReferencedUser?,
    val referencedZap: ReferencedZap?,
    val position: Int,
)

fun EventUriNostr.asNoteNostrUriUi() =
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

fun EventUriNostrReference.asNoteNostrUriUi() =
    NoteNostrUriUi(
        uri = this.uri,
        type = this.type,
        referencedEventAlt = this.referencedEventAlt,
        referencedHighlight = this.referencedHighlight,
        referencedNote = this.referencedNote,
        referencedArticle = this.referencedArticle,
        referencedUser = this.referencedUser,
        referencedZap = referencedZap,
        position = this.position ?: 0,
    )
