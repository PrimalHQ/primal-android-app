package net.primal.android.notes.feed.note.ui.events

import net.primal.domain.nostr.ReactionType

data class NoteCallbacks(
    val onNoteClick: ((noteId: String) -> Unit)? = null,
    val onNoteReplyClick: ((noteNevent: String) -> Unit)? = null,
    val onNoteQuoteClick: ((noteNevent: String) -> Unit)? = null,
    val onHighlightReplyClick: ((highlightNevent: String, articleNaddr: String) -> Unit)? = null,
    val onHighlightQuoteClick: ((highlightNevent: String, articleNaddr: String) -> Unit)? = null,
    val onArticleClick: ((naddr: String) -> Unit)? = null,
    val onArticleReplyClick: ((naddr: String) -> Unit)? = null,
    val onArticleQuoteClick: ((naddr: String) -> Unit)? = null,
    val onProfileClick: ((profileId: String) -> Unit)? = null,
    val onHashtagClick: ((hashtag: String) -> Unit)? = null,
    val onMediaClick: ((event: MediaClickEvent) -> Unit)? = null,
    val onPayInvoiceClick: ((event: InvoicePayClickEvent) -> Unit)? = null,
    val onEventReactionsClick: ((eventId: String, initialTab: ReactionType) -> Unit)? = null,
    val onGetPrimalPremiumClick: (() -> Unit)? = null,
    val onPrimalLegendsLeaderboardClick: (() -> Unit)? = null,
)
