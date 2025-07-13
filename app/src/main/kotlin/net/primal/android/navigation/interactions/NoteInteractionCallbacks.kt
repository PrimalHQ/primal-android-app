package net.primal.android.navigation.interactions

import net.primal.android.notes.feed.note.ui.events.InvoicePayClickEvent
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.domain.nostr.ReactionType

interface NoteInteractionCallbacks {
    fun onNoteClick(noteId: String)
    fun onNoteReplyClick(noteNevent: String)
    fun onNoteQuoteClick(noteNevent: String)
    fun onMediaClick(event: MediaClickEvent)
    fun onEventReactionsClick(
        eventId: String,
        initialTab: ReactionType,
        articleATag: String?,
    )
    fun onPayInvoiceClick(event: InvoicePayClickEvent)
}
