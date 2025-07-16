package net.primal.android.navigation.navigator

import net.primal.android.notes.feed.note.ui.events.InvoicePayClickEvent
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.domain.nostr.ReactionType

object NoOpNavigator : PrimalNavigator {
    override fun onNoteClick(noteId: String) = Unit
    override fun onNoteReplyClick(noteNevent: String) = Unit
    override fun onNoteQuoteClick(noteNevent: String) = Unit
    override fun onMediaClick(event: MediaClickEvent) = Unit
    override fun onEventReactionsClick(
        eventId: String,
        initialTab: ReactionType,
        articleATag: String?,
    ) = Unit
    override fun onPayInvoiceClick(event: InvoicePayClickEvent) = Unit
    override fun onArticleClick(naddr: String) = Unit
    override fun onArticleReplyClick(naddr: String) = Unit
    override fun onArticleQuoteClick(naddr: String) = Unit
    override fun onHighlightReplyClick(highlightNevent: String, articleNaddr: String) = Unit
    override fun onHighlightQuoteClick(highlightNevent: String, articleNaddr: String) = Unit
    override fun onProfileClick(profileId: String) = Unit
    override fun onHashtagClick(hashtag: String) = Unit
    override fun onGetPrimalPremiumClick() = Unit
    override fun onPrimalLegendsLeaderboardClick() = Unit
}
