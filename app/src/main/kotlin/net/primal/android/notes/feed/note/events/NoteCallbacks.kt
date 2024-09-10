package net.primal.android.notes.feed.note.events

data class NoteCallbacks(
    val onNoteClick: ((noteId: String) -> Unit)? = null,
    val onArticleClick: ((naddr: String) -> Unit)? = null,
    val onProfileClick: ((profileId: String) -> Unit)? = null,
    val onNoteReplyClick: ((noteId: String) -> Unit)? = null,
    val onNoteQuoteClick: ((noteId: String) -> Unit)? = null,
    val onHashtagClick: ((hashtag: String) -> Unit)? = null,
    val onMediaClick: ((event: MediaClickEvent) -> Unit)? = null,
    val onPayInvoiceClick: ((event: InvoicePayClickEvent) -> Unit)? = null,
)
