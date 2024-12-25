package net.primal.android.notes.feed.note.ui.events

data class NoteCallbacks(
    val onNoteClick: ((noteId: String) -> Unit)? = null,
    val onNoteReplyClick: ((noteId: String) -> Unit)? = null,
    val onNoteQuoteClick: ((noteId: String) -> Unit)? = null,
    val onHighlightReplyClick: ((highlightNevent: String, articleNaddr: String) -> Unit)? = null,
    val onHighlightQuoteClick: ((highlightNevent: String, articleNaddr: String) -> Unit)? = null,
    val onArticleClick: ((naddr: String) -> Unit)? = null,
    val onArticleReplyClick: ((naddr: String) -> Unit)? = null,
    val onArticleQuoteClick: ((naddr: String) -> Unit)? = null,
    val onProfileClick: ((profileId: String) -> Unit)? = null,
    val onHashtagClick: ((hashtag: String) -> Unit)? = null,
    val onMediaClick: ((event: MediaClickEvent) -> Unit)? = null,
    val onPayInvoiceClick: ((event: InvoicePayClickEvent) -> Unit)? = null,
    val onEventReactionsClick: ((eventId: String) -> Unit)? = null,
    val onGetPrimalPremiumClick: (() -> Unit)? = null,
)
