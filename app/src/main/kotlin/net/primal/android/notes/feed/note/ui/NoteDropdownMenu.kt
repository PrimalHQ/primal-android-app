package net.primal.android.notes.feed.note.ui

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
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextAddBookmark
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteId
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteLink
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteText
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyRawData
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteConversation
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteUser
import net.primal.android.core.compose.icons.primaliconpack.ContextRemoveBookmark
import net.primal.android.core.compose.icons.primaliconpack.ContextReportContent
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.ContextShareImage
import net.primal.android.core.compose.icons.primaliconpack.Delete
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.resolvePrimalNoteLink
import net.primal.android.core.utils.systemShareImage
import net.primal.android.core.utils.systemShareText
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV.toNeventString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.utils.withNostrPrefix

@Composable
fun NoteDropdownMenuIcon(
    modifier: Modifier,
    noteId: String,
    noteContent: String,
    noteRawData: String,
    authorId: String,
    isBookmarked: Boolean,
    isThreadMuted: Boolean,
    isNoteAuthor: Boolean,
    relayHints: List<String> = emptyList(),
    noteGraphicsLayer: GraphicsLayer,
    enabled: Boolean = true,
    onBookmarkClick: (() -> Unit)? = null,
    onMuteUserClick: (() -> Unit)? = null,
    onMuteThreadClick: (() -> Unit)? = null,
    onUnmuteThreadClick: (() -> Unit)? = null,
    onReportContentClick: (() -> Unit)? = null,
    onRequestDeleteClick: (() -> Unit)? = null,
) {
    var menuVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val copyConfirmationText = stringResource(id = R.string.feed_context_copied_toast)

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
            contentDescription = stringResource(id = R.string.accessibility_note_drop_down),
        )

        DropdownPrimalMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false },
        ) {
            if (isThreadMuted) {
                DropdownPrimalMenuItem(
                    trailingIconVector = PrimalIcons.ContextMuteConversation,
                    tint = AppTheme.colorScheme.error,
                    text = stringResource(id = R.string.feed_context_unmute_thread),
                    onClick = {
                        onUnmuteThreadClick?.invoke()
                        menuVisible = false
                    },
                )
            }
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextShare,
                text = stringResource(id = R.string.feed_context_share_note),
                onClick = {
                    systemShareText(
                        context = context,
                        text = resolvePrimalNoteLink(noteId = noteId),
                    )
                    menuVisible = false
                },
            )
            DropdownPrimalMenuItem(
                trailingIconVector = if (isBookmarked) {
                    PrimalIcons.ContextRemoveBookmark
                } else {
                    PrimalIcons.ContextAddBookmark
                },
                text = if (isBookmarked) {
                    stringResource(id = R.string.feed_context_remove_from_bookmark)
                } else {
                    stringResource(id = R.string.feed_context_add_to_bookmark)
                },
                onClick = {
                    onBookmarkClick?.invoke()
                    menuVisible = false
                },
            )
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextShareImage,
                text = stringResource(id = R.string.feed_context_share_note_as_image),
                onClick = {
                    uiScope.launch {
                        runCatching {
                            systemShareImage(
                                context = context,
                                bitmap = noteGraphicsLayer.toImageBitmap().asAndroidBitmap(),
                            )
                        }
                    }
                    menuVisible = false
                },
            )
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextCopyNoteLink,
                text = stringResource(id = R.string.feed_context_copy_note_link),
                onClick = {
                    copyText(context = context, text = resolvePrimalNoteLink(noteId = noteId))
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
                trailingIconVector = PrimalIcons.ContextCopyNoteText,
                text = stringResource(id = R.string.feed_context_copy_note_text),
                onClick = {
                    copyText(context = context, text = noteContent)
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
                trailingIconVector = PrimalIcons.ContextCopyNoteId,
                text = stringResource(id = R.string.feed_context_copy_note_id),
                onClick = {
                    val nevent = Nevent(
                        eventId = noteId,
                        kind = NostrEventKind.ShortTextNote.value,
                        userId = authorId,
                        relays = relayHints,
                    ).toNeventString()
                    copyText(context = context, text = nevent.withNostrPrefix())
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
                trailingIconVector = PrimalIcons.ContextCopyRawData,
                text = stringResource(id = R.string.feed_context_copy_raw_data),
                onClick = {
                    copyText(context = context, text = noteRawData)
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
            if (!isNoteAuthor) {
                DropdownPrimalMenuItem(
                    trailingIconVector = PrimalIcons.ContextMuteUser,
                    tint = AppTheme.colorScheme.error,
                    text = stringResource(id = R.string.feed_context_mute_user),
                    onClick = {
                        onMuteUserClick?.invoke()
                        menuVisible = false
                    },
                )
            }
            if (!isThreadMuted && !isNoteAuthor) {
                DropdownPrimalMenuItem(
                    trailingIconVector = PrimalIcons.ContextMuteConversation,
                    tint = AppTheme.colorScheme.error,
                    text = stringResource(id = R.string.feed_context_mute_thread),
                    onClick = {
                        onMuteThreadClick?.invoke()
                        menuVisible = false
                    },
                )
            }
            if (!isNoteAuthor) {
                DropdownPrimalMenuItem(
                    trailingIconVector = PrimalIcons.ContextReportContent,
                    tint = AppTheme.colorScheme.error,
                    text = stringResource(id = R.string.context_menu_report_content),
                    onClick = {
                        onReportContentClick?.invoke()
                        menuVisible = false
                    },
                )
            }
            if (isNoteAuthor) {
                DropdownPrimalMenuItem(
                    iconSize = 20.dp,
                    trailingIconVector = PrimalIcons.Delete,
                    tint = AppTheme.colorScheme.error,
                    text = stringResource(id = R.string.feed_context_request_delete),
                    onClick = {
                        onRequestDeleteClick?.invoke()
                        menuVisible = false
                    },
                )
            }
        }
    }
}
