package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.toNoteContentUi
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.ext.openUriSafely

@Composable
fun FeedNote(
    data: FeedPostUi,
    fullWidthContent: Boolean,
    avatarSizeDp: Dp,
    avatarPaddingValues: PaddingValues,
    notePaddingValues: PaddingValues,
    headerSingleLine: Boolean,
    showReplyTo: Boolean,
    forceContentIndent: Boolean,
    expanded: Boolean,
    textSelectable: Boolean,
    onPostClick: ((String) -> Unit)? = null,
    onArticleClick: ((naddr: String) -> Unit)? = null,
    onProfileClick: ((String) -> Unit)? = null,
    onHashtagClick: ((String) -> Unit)? = null,
    onMediaClick: ((MediaClickEvent) -> Unit)? = null,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongClickAction: ((FeedPostAction) -> Unit)? = null,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    contentFooter: @Composable () -> Unit = {},
) {
    val localUriHandler = LocalUriHandler.current
    val uiScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    Row {
        if (!fullWidthContent) {
            AvatarThumbnail(
                modifier = Modifier.padding(avatarPaddingValues),
                avatarSize = avatarSizeDp,
                avatarCdnImage = data.authorAvatarCdnImage,
                onClick = if (onProfileClick != null) {
                    { onProfileClick.invoke(data.authorId) }
                } else {
                    null
                },
            )
        }

        Column(
            modifier = Modifier.padding(start = 0.dp),
        ) {
            FeedNoteHeader(
                modifier = Modifier
                    .padding(notePaddingValues)
                    .fillMaxWidth(),
                postTimestamp = data.timestamp,
                singleLine = headerSingleLine,
                authorAvatarVisible = fullWidthContent,
                authorAvatarSize = avatarSizeDp,
                authorDisplayName = data.authorName,
                authorAvatarCdnImage = data.authorAvatarCdnImage,
                authorInternetIdentifier = data.authorInternetIdentifier,
                replyToAuthor = if (showReplyTo) data.replyToAuthorHandle else null,
                onAuthorAvatarClick = if (onProfileClick != null) {
                    { onProfileClick(data.authorId) }
                } else {
                    null
                },
            )

            val postAuthorGuessHeight = with(LocalDensity.current) { 128.dp.toPx() }
            val launchRippleEffect: (Offset) -> Unit = {
                uiScope.launch {
                    val press = PressInteraction.Press(it.copy(y = it.y + postAuthorGuessHeight))
                    interactionSource.emit(press)
                    interactionSource.emit(PressInteraction.Release(press))
                }
            }

            NoteContent(
                modifier = Modifier
                    .padding(horizontal = if (fullWidthContent) 10.dp else 8.dp)
                    .padding(start = if (forceContentIndent && fullWidthContent) avatarSizeDp + 6.dp else 0.dp)
                    .padding(top = if (fullWidthContent || !headerSingleLine) 10.dp else 5.dp),
                data = data.toNoteContentUi(),
                expanded = expanded,
                textSelectable = textSelectable,
                onClick = if (onPostClick != null) {
                    {
                        launchRippleEffect(it)
                        onPostClick.invoke(data.postId)
                    }
                } else {
                    null
                },
                onProfileClick = onProfileClick,
                onPostClick = onPostClick,
                onArticleClick = onArticleClick,
                onUrlClick = {
                    localUriHandler.openUriSafely(it)
                },
                onHashtagClick = onHashtagClick,
                onMediaClick = onMediaClick,
                onPayInvoiceClick = onPayInvoiceClick,
            )

            contentFooter()

            FeedNoteStatsRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(top = 8.dp)
                    .padding(bottom = 8.dp),
                eventStats = data.stats,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongClickAction,
            )
        }
    }
}
