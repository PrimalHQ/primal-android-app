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
import net.primal.android.core.compose.icons.primaliconpack.ContextReportUser
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.ContextShowHighlightsOutlined
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.resolvePrimalArticleLink
import net.primal.android.core.utils.systemShareText
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.profile.report.ReportType
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind

@ExperimentalMaterial3Api
@Composable
fun ArticleDropdownMenuIcon(
    modifier: Modifier,
    articleId: String,
    articleContent: String?,
    articleRawData: String?,
    authorId: String,
    isBookmarked: Boolean,
    enabled: Boolean = true,
    showHighlights: Boolean? = null,
    onToggleHighlightsClick: (() -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    onMuteUserClick: (() -> Unit)? = null,
    onReportContentClick: ((reportType: ReportType) -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    var menuVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val copyConfirmationText = stringResource(id = R.string.feed_context_copied_toast)

    val naddr = Naddr(
        identifier = articleId,
        userId = authorId,
        kind = NostrEventKind.LongFormContent.value,
    ).toNaddrString()

    var reportDialogVisible by remember { mutableStateOf(false) }
    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReportContentClick?.invoke(type)
            },
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

        DropdownPrimalMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false },
        ) {
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextShare,
                text = stringResource(id = R.string.article_feed_context_share_article),
                onClick = {
                    systemShareText(
                        context = context,
                        text = resolvePrimalArticleLink(naddr = naddr),
                    )
                    menuVisible = false
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
                        onToggleHighlightsClick?.invoke()
                        menuVisible = false
                    },
                )
            }
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextCopyNoteLink,
                text = stringResource(id = R.string.article_feed_context_copy_article_link),
                onClick = {
                    copyText(context = context, text = resolvePrimalArticleLink(naddr = naddr))
                    menuVisible = false
                    uiScope.launch {
                        Toast.makeText(
                            context,
                            copyConfirmationText,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
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
                    onBookmarkClick?.invoke()
                    menuVisible = false
                },
            )
            if (!articleContent.isNullOrEmpty()) {
                DropdownPrimalMenuItem(
                    trailingIconVector = PrimalIcons.ContextCopyNoteText,
                    text = stringResource(id = R.string.article_feed_context_copy_article_text),
                    onClick = {
                        copyText(context = context, text = articleContent)
                        menuVisible = false
                        uiScope.launch {
                            Toast.makeText(
                                context,
                                copyConfirmationText,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }

            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextCopyNoteId,
                text = stringResource(id = R.string.article_feed_context_copy_article_id),
                onClick = {
                    copyText(context = context, text = naddr)
                    menuVisible = false
                    uiScope.launch {
                        Toast.makeText(
                            context,
                            copyConfirmationText,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )

            if (articleRawData != null) {
                DropdownPrimalMenuItem(
                    trailingIconVector = PrimalIcons.ContextCopyRawData,
                    text = stringResource(id = R.string.article_feed_context_copy_raw_data),
                    onClick = {
                        copyText(context = context, text = articleRawData)
                        menuVisible = false
                        uiScope.launch {
                            Toast.makeText(
                                context,
                                copyConfirmationText,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }

            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextCopyPublicKey,
                text = stringResource(id = R.string.article_feed_context_copy_user_id),
                onClick = {
                    copyText(context = context, text = authorId.hexToNpubHrp())
                    menuVisible = false
                    uiScope.launch {
                        Toast.makeText(
                            context,
                            copyConfirmationText,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )

            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextMuteUser,
                tint = AppTheme.colorScheme.error,
                text = stringResource(id = R.string.context_menu_mute_user),
                onClick = {
                    onMuteUserClick?.invoke()
                    menuVisible = false
                },
            )

            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextReportUser,
                tint = AppTheme.colorScheme.error,
                text = stringResource(id = R.string.context_menu_report_content),
                onClick = {
                    menuVisible = false
                    reportDialogVisible = true
                },
            )
        }
    }
}
