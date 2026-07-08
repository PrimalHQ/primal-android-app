package net.primal.android.articles.feed.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextAddBookmark
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteId
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteLink
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteText
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyPublicKey
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyRawData
import net.primal.android.core.compose.icons.primaliconpack.ContextHideHighlightsOutlined
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteUser
import net.primal.android.core.compose.icons.primaliconpack.ContextRemoveBookmark
import net.primal.android.core.compose.icons.primaliconpack.ContextReportContent
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.ContextShowHighlightsOutlined
import net.primal.android.core.compose.icons.primaliconpack.Delete
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.systemShareText
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.utils.withNostrPrefix

@ExperimentalMaterial3Api
@Composable
fun ArticleDropdownMenu(
    modifier: Modifier,
    articleId: String,
    eventId: String,
    articleATag: String,
    authorId: String,
    isBookmarked: Boolean,
    shareUrl: String,
    enabled: Boolean = true,
    isArticleAuthor: Boolean,
    showHighlights: Boolean? = null,
    onToggleHighlightsClick: (() -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    onMuteUserClick: (() -> Unit)? = null,
    onRequestDeleteClick: ((eventId: String, articleATag: String, authorId: String) -> Unit)? = null,
    onReportContentClick: ((reportType: ReportType) -> Unit)? = null,
    onCopyArticleTextClick: (() -> Unit)? = null,
    onCopyRawDataClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    var menuVisible by remember { mutableStateOf(false) }
    var reportDialogVisible by remember { mutableStateOf(false) }
    var deleteDialogVisible by remember { mutableStateOf(false) }

    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReportContentClick?.invoke(type)
            },
        )
    }

    if (deleteDialogVisible && onRequestDeleteClick != null) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.context_confirm_delete_positive),
            dismissText = stringResource(id = R.string.context_confirm_delete_negative),
            dialogTitle = stringResource(id = R.string.context_confirm_delete_article_title),
            dialogText = stringResource(id = R.string.context_confirm_delete_article_text),
            onConfirmation = {
                deleteDialogVisible = false
                onRequestDeleteClick(eventId, articleATag, authorId)
            },
            onDismissRequest = { deleteDialogVisible = false },
        )
    }

    Box(
        modifier = modifier.clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { menuVisible = true },
        ),
    ) {
        icon()

        ArticleDropdownPrimalMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false },
            articleId = articleId,
            authorId = authorId,
            onCopyArticleTextClick = onCopyArticleTextClick,
            onCopyRawDataClick = onCopyRawDataClick,
            isBookmarked = isBookmarked,
            isArticleAuthor = isArticleAuthor,
            showHighlights = showHighlights,
            shareUrl = shareUrl,
            onToggleHighlightsClick = { onToggleHighlightsClick?.invoke() },
            onBookmarkClick = { onBookmarkClick?.invoke() },
            onMuteUserClick = { onMuteUserClick?.invoke() },
            onShowReportDialog = { reportDialogVisible = true },
            onShowDeleteDialog = { deleteDialogVisible = true },
        )
    }
}

@Composable
private fun ArticleDropdownPrimalMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    articleId: String,
    authorId: String,
    onCopyArticleTextClick: (() -> Unit)?,
    onCopyRawDataClick: (() -> Unit)?,
    isBookmarked: Boolean,
    isArticleAuthor: Boolean,
    showHighlights: Boolean?,
    shareUrl: String,
    onToggleHighlightsClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMuteUserClick: () -> Unit,
    onShowReportDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
) {
    DropdownPrimalMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        val context = LocalContext.current

        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextShare,
            text = stringResource(id = R.string.article_feed_context_share_article),
            onClick = {
                systemShareText(context = context, text = shareUrl)
                onDismissRequest()
            },
        )

        DropdownPrimalMenuItem(
            trailingIconVector = if (isBookmarked) {
                PrimalIcons.ContextRemoveBookmark
            } else {
                PrimalIcons.ContextAddBookmark
            },
            text = if (isBookmarked) {
                stringResource(id = R.string.article_feed_context_remove_from_bookmark)
            } else {
                stringResource(id = R.string.article_feed_context_add_to_bookmark)
            },
            onClick = {
                onBookmarkClick()
                onDismissRequest()
            },
        )

        if (showHighlights != null) {
            DropdownPrimalMenuItem(
                trailingIconVector = if (showHighlights) {
                    PrimalIcons.ContextHideHighlightsOutlined
                } else {
                    PrimalIcons.ContextShowHighlightsOutlined
                },
                text = if (showHighlights) {
                    stringResource(id = R.string.article_feed_context_hide_highglights)
                } else {
                    stringResource(id = R.string.article_feed_context_show_highglights)
                },
                onClick = {
                    onToggleHighlightsClick()
                    onDismissRequest()
                },
            )
        }

        CopyMenuItems(
            articleId = articleId,
            authorId = authorId,
            shareUrl = shareUrl,
            onCopyArticleTextClick = onCopyArticleTextClick,
            onCopyRawDataClick = onCopyRawDataClick,
            onDismissRequest = onDismissRequest,
        )

        ContentModerationMenuItems(
            isArticleAuthor = isArticleAuthor,
            onMuteUserClick = onMuteUserClick,
            onDismissRequest = onDismissRequest,
            onShowReportDialog = onShowReportDialog,
            onShowDeleteDialog = onShowDeleteDialog,
        )
    }
}

@Composable
private fun CopyMenuItems(
    articleId: String,
    authorId: String,
    shareUrl: String,
    onCopyArticleTextClick: (() -> Unit)?,
    onCopyRawDataClick: (() -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val copyConfirmationText = stringResource(id = R.string.feed_context_copied_toast)

    fun showCopiedToast() {
        uiScope.launch {
            Toast.makeText(context, copyConfirmationText, Toast.LENGTH_SHORT).show()
        }
    }

    val naddr = Naddr(
        identifier = articleId,
        userId = authorId,
        kind = NostrEventKind.LongFormContent.value,
    ).toNaddrString()

    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextCopyNoteLink,
        text = stringResource(id = R.string.article_feed_context_copy_article_link),
        onClick = {
            context.copyText(text = shareUrl)
            showCopiedToast()
            onDismissRequest()
        },
    )

    if (onCopyArticleTextClick != null) {
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextCopyNoteText,
            text = stringResource(id = R.string.article_feed_context_copy_article_text),
            onClick = {
                onCopyArticleTextClick()
                onDismissRequest()
            },
        )
    }

    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextCopyNoteId,
        text = stringResource(id = R.string.article_feed_context_copy_article_id),
        onClick = {
            context.copyText(text = naddr.withNostrPrefix())
            showCopiedToast()
            onDismissRequest()
        },
    )

    if (onCopyRawDataClick != null) {
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextCopyRawData,
            text = stringResource(id = R.string.article_feed_context_copy_raw_data),
            onClick = {
                onCopyRawDataClick()
                onDismissRequest()
            },
        )
    }

    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextCopyPublicKey,
        text = stringResource(id = R.string.article_feed_context_copy_user_id),
        onClick = {
            context.copyText(text = authorId.hexToNpubHrp().withNostrPrefix())
            showCopiedToast()
            onDismissRequest()
        },
    )
}

@Composable
private fun ContentModerationMenuItems(
    isArticleAuthor: Boolean,
    onMuteUserClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onShowReportDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
) {
    if (!isArticleAuthor) {
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextMuteUser,
            tint = AppTheme.colorScheme.error,
            text = stringResource(id = R.string.context_menu_mute_user),
            onClick = {
                onMuteUserClick()
                onDismissRequest()
            },
        )

        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextReportContent,
            tint = AppTheme.colorScheme.error,
            text = stringResource(id = R.string.context_menu_report_content),
            onClick = {
                onShowReportDialog()
                onDismissRequest()
            },
        )
    }

    if (isArticleAuthor) {
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.Delete,
            tint = AppTheme.colorScheme.error,
            text = stringResource(id = R.string.article_feed_context_request_delete),
            onClick = {
                onShowDeleteDialog()
                onDismissRequest()
            },
        )
    }
}
