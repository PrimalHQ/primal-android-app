package net.primal.android.notes.feed.model

import net.primal.domain.links.EventUriNostrReference
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedArticle
import net.primal.domain.links.ReferencedHighlight
import net.primal.domain.links.ReferencedNote
import net.primal.domain.links.ReferencedUser
import net.primal.domain.links.ReferencedZap

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
