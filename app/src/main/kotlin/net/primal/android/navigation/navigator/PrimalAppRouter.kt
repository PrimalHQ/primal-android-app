package net.primal.android.navigation.navigator

import androidx.navigation.NavController
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.navigation.navigateToArticleDetails
import net.primal.android.navigation.navigateToExploreFeed
import net.primal.android.navigation.navigateToMediaGallery
import net.primal.android.navigation.navigateToNoteEditor
import net.primal.android.navigation.navigateToPremiumBuying
import net.primal.android.navigation.navigateToPremiumLegendLeaderboard
import net.primal.android.navigation.navigateToProfile
import net.primal.android.navigation.navigateToReactions
import net.primal.android.navigation.navigateToThread
import net.primal.android.navigation.navigateToWalletCreateTransaction
import net.primal.android.notes.feed.note.ui.events.InvoicePayClickEvent
import net.primal.android.notes.feed.note.ui.events.MediaClickEvent
import net.primal.domain.feeds.buildAdvancedSearchNotesFeedSpec
import net.primal.domain.nostr.ReactionType

internal class PrimalAppRouter(private val navController: NavController) : PrimalNavigator {
    // --- NoteInteractionCallbacks Implementation ---
    override fun onNoteClick(noteId: String) = navController.navigateToThread(noteId)
    override fun onNoteReplyClick(noteNevent: String) =
        navController.navigateToNoteEditor(NoteEditorArgs(referencedNoteNevent = noteNevent))
    override fun onNoteQuoteClick(noteNevent: String) =
        navController.navigateToNoteEditor(args = NoteEditorArgs(referencedNoteNevent = noteNevent, isQuoting = true))
    override fun onMediaClick(event: MediaClickEvent) =
        navController.navigateToMediaGallery(
            noteId = event.noteId,
            mediaUrl = event.mediaUrl,
            mediaPositionMs = event.positionMs,
        )
    override fun onEventReactionsClick(
        eventId: String,
        initialTab: ReactionType,
        articleATag: String?,
    ) = navController.navigateToReactions(eventId, initialTab, articleATag)
    override fun onPayInvoiceClick(event: InvoicePayClickEvent) =
        navController.navigateToWalletCreateTransaction(lnbc = event.lnbc)

    // --- ArticleInteractionCallbacks Implementation ---
    override fun onArticleClick(naddr: String) = navController.navigateToArticleDetails(naddr)
    override fun onArticleReplyClick(naddr: String) =
        navController.navigateToNoteEditor(NoteEditorArgs(referencedArticleNaddr = naddr))
    override fun onArticleQuoteClick(naddr: String) =
        navController.navigateToNoteEditor(args = NoteEditorArgs(referencedArticleNaddr = naddr, isQuoting = true))

    override fun onHighlightReplyClick(highlightNevent: String, articleNaddr: String) {
        navController.navigateToNoteEditor(
            args = NoteEditorArgs(
                referencedHighlightNevent = highlightNevent,
                referencedArticleNaddr = articleNaddr,
            ),
        )
    }

    override fun onHighlightQuoteClick(highlightNevent: String, articleNaddr: String) {
        navController.navigateToNoteEditor(
            args = NoteEditorArgs(
                referencedArticleNaddr = articleNaddr,
                referencedHighlightNevent = highlightNevent,
                isQuoting = true,
            ),
        )
    }

    // --- ContentInteractionCallbacks Implementation ---
    override fun onProfileClick(profileId: String) = navController.navigateToProfile(profileId)
    override fun onHashtagClick(hashtag: String) =
        navController.navigateToExploreFeed(feedSpec = buildAdvancedSearchNotesFeedSpec(query = hashtag))

    // --- PrimalSubscriptionsInteractionCallbacks Implementation ---
    override fun onGetPrimalPremiumClick() = navController.navigateToPremiumBuying()
    override fun onPrimalLegendsLeaderboardClick() = navController.navigateToPremiumLegendLeaderboard()
}
