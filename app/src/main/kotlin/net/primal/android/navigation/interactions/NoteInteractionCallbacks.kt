package net.primal.android.navigation.interactions

import net.primal.android.notes.feed.note.ui.events.InvoicePayClickEvent
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.domain.nostr.ReactionType

data class NoteInteractionCallbacks(
    val onNoteClick: (noteId: String) -> Unit,
    val onNoteReplyClick: (noteNevent: String) -> Unit,
    val onNoteQuoteClick: (noteNevent: String) -> Unit,
    val onMediaClick: (event: MediaClickEvent) -> Unit,
    val onEventReactionsClick: (eventId: String, initialTab: ReactionType, articleATag: String?) -> Unit,
    val onPayInvoiceClick: (event: InvoicePayClickEvent) -> Unit,
)
