package net.primal.android.core.compose.feed

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.DropdownMenuItemText
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteId
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteLink
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteText
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyPublicKey
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyRawData
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.resolvePrimalNoteLink
import net.primal.android.core.utils.systemShareText
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun FeedPostListItem(
    data: FeedPostUi,
    shape: Shape = CardDefaults.shape,
    cardPadding: PaddingValues = PaddingValues(horizontal = 4.dp),
    shouldIndentContent: Boolean = false,
    connectedToPreviousNote: Boolean = false,
    connectedToNextNote: Boolean = false,
    highlighted: Boolean = false,
    expanded: Boolean = false,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostAction: (FeedPostAction) -> Unit,
    onPostLongClickAction: (FeedPostAction) -> Unit,
    onHashtagClick: (String) -> Unit,
) {
    val localUriHandler = LocalUriHandler.current
    val uiScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    CardWithHighlight(
        modifier = Modifier
            .wrapContentHeight()
            .padding(cardPadding)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { onPostClick(data.postId) },
            ),
        shape = shape,
        highlighted = highlighted,
        connectedToPreviousNote = connectedToPreviousNote,
        connectedToNextNote = connectedToNextNote,
    ) {
        if (data.repostAuthorName != null) {
            RepostedNotice(
                repostedBy = data.repostAuthorName,
                onRepostAuthorClick = {
                    if (data.repostAuthorId != null) {
                        onProfileClick(data.repostAuthorId)
                    }
                }
            )
        }

        Box(
            contentAlignment = Alignment.TopEnd,
        ) {
            FeedPostAuthorRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, bottom = 8.dp, end = 24.dp),
                authorDisplayName = data.authorName,
                postTimestamp = data.timestamp,
                authorAvatarUrl = data.authorAvatarUrl,
                authorResources = data.authorMediaResources,
                authorInternetIdentifier = data.authorInternetIdentifier,
                onAuthorAvatarClick = { onProfileClick(data.authorId) },
            )

            NoteDropdownMenu(
                noteId = data.postId,
                noteContent = data.content,
                noteRawData = data.rawNostrEventJson,
                authorId = data.authorId,
            )
        }

        val postAuthorGuessHeight = with(LocalDensity.current) { 128.dp.toPx() }
        val launchRippleEffect: (Offset) -> Unit = {
            uiScope.launch {
                val press = PressInteraction.Press(it.copy(y = it.y + postAuthorGuessHeight))
                interactionSource.emit(press)
                interactionSource.emit(PressInteraction.Release(press))
            }
        }

        Column(
            modifier = Modifier.padding(start = if (shouldIndentContent) 64.dp else 0.dp),
        ) {
            FeedPostContent(
                modifier = Modifier.padding(horizontal = 16.dp),
                content = data.content,
                expanded = expanded,
                hashtags = data.hashtags,
                mediaResources = data.mediaResources,
                nostrResources = data.nostrResources,
                onClick = {
                    launchRippleEffect(it)
                    onPostClick(data.postId)
                },
                onProfileClick = onProfileClick,
                onPostClick = onPostClick,
                onUrlClick = {
                    localUriHandler.openUriSafely(it)
                },
                onHashtagClick = onHashtagClick,
            )

            FeedPostStatsRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 16.dp),
                postStats = data.stats,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongClickAction,
            )
        }
    }
}

@Composable
private fun NoteDropdownMenu(
    noteId: String,
    noteContent: String,
    noteRawData: String,
    authorId: String,
) {
    var menuVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val copyConfirmationText = stringResource(id = R.string.feed_context_copied_toast)

    IconButton(
        onClick = { menuVisible = true },
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = PrimalIcons.More,
            contentDescription = null,
        )

        DropdownMenu(
            modifier = Modifier.background(color = AppTheme.extraColorScheme.surfaceVariantAlt),
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false },
        ) {
            DropdownMenuItem(
                trailingIcon = { Icon(imageVector = PrimalIcons.ContextShare, contentDescription = null)},
                text = { DropdownMenuItemText(text = stringResource(id = R.string.feed_context_share_note)) },
                onClick = {
                    systemShareText(context = context, text = resolvePrimalNoteLink(noteId = noteId))
                    menuVisible = false
                }
            )
            DropdownMenuItem(
                trailingIcon = { Icon(imageVector = PrimalIcons.ContextCopyNoteLink, contentDescription = null)},
                text = { DropdownMenuItemText(text = stringResource(id = R.string.feed_context_copy_note_link)) },
                onClick = {
                    copyText(context = context, text = resolvePrimalNoteLink(noteId = noteId))
                    menuVisible = false
                    uiScope.launch { Toast.makeText(context, copyConfirmationText, Toast.LENGTH_SHORT).show() }
                }
            )
            DropdownMenuItem(
                trailingIcon = { Icon(imageVector = PrimalIcons.ContextCopyNoteText, contentDescription = null)},
                text = { DropdownMenuItemText(text = stringResource(id = R.string.feed_context_copy_note_text)) },
                onClick = {
                    copyText(context = context, text = noteContent)
                    menuVisible = false
                    uiScope.launch { Toast.makeText(context, copyConfirmationText, Toast.LENGTH_SHORT).show() }
                }
            )
            DropdownMenuItem(
                trailingIcon = { Icon(imageVector = PrimalIcons.ContextCopyNoteId, contentDescription = null)},
                text = { DropdownMenuItemText(text = stringResource(id = R.string.feed_context_copy_note_id)) },
                onClick = {
                    copyText(context = context, text = noteId.hexToNoteHrp())
                    menuVisible = false
                    uiScope.launch { Toast.makeText(context, copyConfirmationText, Toast.LENGTH_SHORT).show() }
                }
            )
            DropdownMenuItem(
                trailingIcon = { Icon(imageVector = PrimalIcons.ContextCopyRawData, contentDescription = null)},
                text = { DropdownMenuItemText(text = stringResource(id = R.string.feed_context_copy_raw_data)) },
                onClick = {
                    copyText(context = context, text = noteRawData)
                    menuVisible = false
                    uiScope.launch { Toast.makeText(context, copyConfirmationText, Toast.LENGTH_SHORT).show() }
                }
            )
            DropdownMenuItem(
                trailingIcon = { Icon(imageVector = PrimalIcons.ContextCopyPublicKey, contentDescription = null)},
                text = { DropdownMenuItemText(text = stringResource(id = R.string.feed_context_copy_user_id)) },
                onClick = {
                    copyText(context = context, text = authorId.hexToNpubHrp())
                    menuVisible = false
                    uiScope.launch { Toast.makeText(context, copyConfirmationText, Toast.LENGTH_SHORT).show() }
                }
            )
        }
    }
}

@Preview
@Composable
fun PreviewFeedPostListItemLight() {
    PrimalTheme(primalTheme = PrimalTheme.Sunrise) {
        FeedPostListItem(
            data = FeedPostUi(
                postId = "random",
                repostId = "repostRandom",
                repostAuthorId = "repostId",
                repostAuthorName = "jack",
                content = """
                    Unfortunately the days of using pseudonyms in metaspace are numbered. #nostr 

                    It won't be long before non-trivial numbers of individuals and businesses 
                    have augmented reality HUDs that incorporate real-time facial recognition. 
                    Hiding behind a pseudonym will become a distant dream.
                """.trimIndent(),
                mediaResources = emptyList(),
                authorId = "npubSomething",
                authorName = "android_robots_from_space",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarUrl = "https://i.imgur.com/Z8dpmvc.png",
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                authorMediaResources = emptyList(),
                nostrResources = emptyList(),
                stats = FeedPostStatsUi(
                    repliesCount = 11,
                    likesCount = 256,
                    userLiked = true,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
            ),
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewFeedPostListItemDark() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        FeedPostListItem(
            data = FeedPostUi(
                postId = "random",
                repostId = "repostRandom",
                repostAuthorId = "repostId",
                repostAuthorName = "jack",
                content = """
                    Unfortunately the days of using pseudonyms in metaspace are numbered. #nostr

                    It won't be long before non-trivial numbers of individuals and businesses 
                    have augmented reality HUDs that incorporate real-time facial recognition. 
                    Hiding behind a pseudonym will become a distant dream.
                """.trimIndent(),
                mediaResources = emptyList(),
                authorId = "npubSomething",
                authorName = "android",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarUrl = "https://i.imgur.com/Z8dpmvc.png",
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                authorMediaResources = emptyList(),
                nostrResources = emptyList(),
                stats = FeedPostStatsUi(
                    repliesCount = 11,
                    userReplied = true,
                    likesCount = 256,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
            ),
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
        )
    }

}
