package net.primal.android.notes.feed.note

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.profile.approvals.ApproveBookmarkAlertDialog
import net.primal.android.notes.feed.NoteRepostOrQuoteBottomSheet
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNeventString
import net.primal.android.notes.feed.note.NoteContract.UiEvent
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.feed.zaps.UnableToZapBottomSheet
import net.primal.android.notes.feed.zaps.ZapBottomSheet
import net.primal.android.profile.report.ReportUserDialog
import net.primal.domain.utils.canZap

class NoteCardDialogsState {
    var showCantZapWarning by mutableStateOf(false)
    var showZapOptions by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)
    var showReportDialog by mutableStateOf(false)
    var showRepostConfirmation by mutableStateOf(false)
}

@Composable
fun rememberNoteCardDialogsState(): NoteCardDialogsState = remember { NoteCardDialogsState() }

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCardDialogs(
    dialogsState: NoteCardDialogsState,
    data: FeedPostUi,
    noteState: NoteContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: (() -> Unit)?,
) {
    if (dialogsState.showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = noteState.zappingState,
            onDismissRequest = { dialogsState.showCantZapWarning = false },
            onGoToWallet = { onGoToWallet?.invoke() },
        )
    }

    if (dialogsState.showZapOptions) {
        ZapBottomSheet(
            onDismissRequest = { dialogsState.showZapOptions = false },
            receiverName = data.authorName,
            zappingState = noteState.zappingState,
            onZap = { zapAmount, zapDescription ->
                if (noteState.zappingState.canZap(zapAmount)) {
                    eventPublisher(
                        UiEvent.ZapAction(
                            postId = data.postId,
                            postAuthorId = data.authorId,
                            zapAmount = zapAmount.toULong(),
                            zapDescription = zapDescription,
                        ),
                    )
                } else {
                    dialogsState.showCantZapWarning = true
                }
            },
        )
    }

    if (dialogsState.showDeleteDialog) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.context_confirm_delete_positive),
            dismissText = stringResource(id = R.string.context_confirm_delete_negative),
            dialogTitle = stringResource(id = R.string.context_confirm_delete_note_title),
            dialogText = stringResource(id = R.string.context_confirm_delete_note_text),
            onConfirmation = {
                dialogsState.showDeleteDialog = false
                eventPublisher(UiEvent.RequestDeleteAction(noteId = data.postId, userId = data.authorId))
            },
            onDismissRequest = { dialogsState.showDeleteDialog = false },
        )
    }

    if (dialogsState.showReportDialog) {
        ReportUserDialog(
            onDismissRequest = { dialogsState.showReportDialog = false },
            onReportClick = { type ->
                dialogsState.showReportDialog = false
                eventPublisher(
                    UiEvent.ReportAbuse(
                        reportType = type,
                        profileId = data.authorId,
                        noteId = data.postId,
                    ),
                )
            },
        )
    }

    if (dialogsState.showRepostConfirmation) {
        NoteRepostOrQuoteBottomSheet(
            isReposted = data.stats.userReposted,
            onDismiss = { dialogsState.showRepostConfirmation = false },
            onRepostClick = {
                eventPublisher(
                    UiEvent.RepostAction(
                        postId = data.postId,
                        postAuthorId = data.authorId,
                        postNostrEvent = data.rawNostrEventJson,
                    ),
                )
            },
            onDeleteRepostClick = {
                eventPublisher(
                    UiEvent.DeleteRepostAction(
                        postId = data.postId,
                        repostId = data.repostId,
                        repostAuthorId = data.repostAuthorId,
                    ),
                )
            },
            onPostQuoteClick = {
                noteCallbacks.onNoteQuoteClick?.invoke(data.asNeventString())
            },
        )
    }

    if (noteState.shouldApproveBookmark) {
        ApproveBookmarkAlertDialog(
            onBookmarkConfirmed = {
                eventPublisher(
                    UiEvent.BookmarkAction(
                        noteId = data.postId,
                        forceUpdate = true,
                    ),
                )
            },
            onClose = {
                eventPublisher(UiEvent.DismissBookmarkConfirmation(noteId = data.postId))
            },
        )
    }
}
