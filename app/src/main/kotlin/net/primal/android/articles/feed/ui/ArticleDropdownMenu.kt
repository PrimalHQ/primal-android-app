package net.primal.android.articles.feed.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteId
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteLink
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteText
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyPublicKey
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyRawData
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.resolvePrimalArticleLink
import net.primal.android.core.utils.systemShareText
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString

@Composable
fun ArticleDropdownMenuIcon(
    modifier: Modifier,
    articleId: String,
    articleContent: String?,
    articleRawData: String?,
    authorId: String,
    isBookmarked: Boolean,
    enabled: Boolean = true,
    onBookmarkClick: (() -> Unit)? = null,
    onMuteUserClick: (() -> Unit)? = null,
    onReportContentClick: (() -> Unit)? = null,
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

    Box(
        modifier = modifier.clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { menuVisible = true },
        ),
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 14.dp, end = 8.dp)
                .wrapContentSize(align = Alignment.TopEnd),
            imageVector = PrimalIcons.More,
            contentDescription = stringResource(id = R.string.accessibility_article_drop_down),
        )

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
//            DropdownPrimalMenuItem(
//                trailingIconVector = if (isBookmarked) {
//                    PrimalIcons.ContextRemoveBookmark
//                } else {
//                    PrimalIcons.ContextAddBookmark
//                },
//                text = if (isBookmarked) {
//                    stringResource(id = R.string.article_feed_context_remove_from_bookmark)
//                } else {
//                    stringResource(id = R.string.article_feed_context_add_to_bookmark)
//                },
//                onClick = {
//                    onBookmarkClick?.invoke()
//                    menuVisible = false
//                },
//            )
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
//            DropdownPrimalMenuItem(
//                trailingIconVector = PrimalIcons.ContextMuteUser,
//                tint = AppTheme.colorScheme.error,
//                text = stringResource(id = R.string.article_context_menu_mute_user),
//                onClick = {
//                    onMuteUserClick?.invoke()
//                    menuVisible = false
//                },
//            )
//            DropdownPrimalMenuItem(
//                trailingIconVector = PrimalIcons.ContextReportUser,
//                tint = AppTheme.colorScheme.error,
//                text = stringResource(id = R.string.article_context_menu_report_content),
//                onClick = {
//                    onReportContentClick?.invoke()
//                    menuVisible = false
//                },
//            )
        }
    }
}
