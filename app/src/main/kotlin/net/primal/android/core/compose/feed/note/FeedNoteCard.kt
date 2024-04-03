package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.temporal.ChronoUnit
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.profile.report.OnReportContentClick
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedNoteCard(
    data: FeedPostUi,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    cardPadding: PaddingValues = PaddingValues(all = 0.dp),
    fullWidthNote: Boolean = false,
    headerSingleLine: Boolean = true,
    fullWidthContent: Boolean = false,
    forceContentIndent: Boolean = false,
    drawLineAboveAvatar: Boolean = false,
    drawLineBelowAvatar: Boolean = false,
    expanded: Boolean = false,
    showReplyTo: Boolean = true,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongClickAction: ((FeedPostAction) -> Unit)? = null,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onBookmarkClick: () -> Unit,
    onMuteUserClick: () -> Unit,
    onReportContentClick: OnReportContentClick,
) {
    val interactionSource = remember { MutableInteractionSource() }

    var reportDialogVisible by remember { mutableStateOf(false) }
    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = {
                reportDialogVisible = false
                onReportContentClick(it, data.authorId, data.postId)
            },
        )
    }

    val notePaddingDp = 4.dp
    val avatarPaddingDp = 8.dp
    val avatarSizeDp = if (fullWidthNote) 30.dp else 42.dp
    val overflowIconSizeDp = 40.dp

    NoteSurfaceCard(
        modifier = modifier
            .wrapContentHeight()
            .padding(cardPadding)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { onPostClick(data.postId) },
            ),
        shape = shape,
        colors = colors,
        drawLineAboveAvatar = drawLineAboveAvatar,
        drawLineBelowAvatar = drawLineBelowAvatar,
        lineOffsetX = (avatarSizeDp / 2) + avatarPaddingDp + notePaddingDp,
    ) {
        Box(
            modifier = Modifier.padding(all = notePaddingDp),
            contentAlignment = Alignment.TopEnd,
        ) {
            // TODO Handle reading isBookmarked state
            NoteDropdownMenuIcon(
                modifier = Modifier
                    .size(overflowIconSizeDp)
                    .padding(all = 8.dp)
                    .clip(CircleShape),
                noteId = data.postId,
                noteContent = data.content,
                noteRawData = data.rawNostrEventJson,
                authorId = data.authorId,
                isBookmarked = false,
                onBookmarkClick = onBookmarkClick,
                onMuteUserClick = onMuteUserClick,
                onReportContentClick = { reportDialogVisible = true },
            )

            Column {
                if (data.repostAuthorName != null) {
                    RepostedNotice(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = avatarPaddingDp)
                            .padding(top = 4.dp),
                        repostedByAuthor = data.repostAuthorName,
                        onRepostAuthorClick = {
                            if (data.repostAuthorId != null) {
                                onProfileClick(data.repostAuthorId)
                            }
                        },
                    )
                }

                FeedNote(
                    fullWidthContent = fullWidthContent,
                    avatarSizeDp = avatarSizeDp,
                    avatarPaddingValues = PaddingValues(start = avatarPaddingDp, top = avatarPaddingDp),
                    notePaddingValues = PaddingValues(
                        start = 8.dp,
                        top = avatarPaddingDp,
                        end = overflowIconSizeDp - 8.dp,
                    ),
                    data = data,
                    headerSingleLine = headerSingleLine,
                    showReplyTo = showReplyTo,
                    forceContentIndent = forceContentIndent,
                    expanded = expanded,
                    onProfileClick = onProfileClick,
                    onPostClick = onPostClick,
                    onHashtagClick = onHashtagClick,
                    onMediaClick = onMediaClick,
                    onPostAction = onPostAction,
                    onPostLongClickAction = onPostLongClickAction,
                )
            }
        }
    }
}

class FeedPostUiProvider : PreviewParameterProvider<FeedPostUi> {
    override val values: Sequence<FeedPostUi>
        get() = sequenceOf(
            FeedPostUi(
                postId = "random",
                content = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                """.trimIndent(),
                attachments = emptyList(),
                authorId = "npubSomething",
                authorName = "android_robots_from_space",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = FeedPostStatsUi(
                    repliesCount = 0,
                    likesCount = 0,
                    userLiked = false,
                    repostsCount = 0,
                    satsZapped = 0,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
                replyToAuthorHandle = "alex",
            ),
            FeedPostUi(
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
                attachments = emptyList(),
                authorId = "npubSomething",
                authorName = "android_robots_from_space",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarCdnImage = CdnImage(sourceUrl = "https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = FeedPostStatsUi(
                    repliesCount = 11,
                    likesCount = 256,
                    userLiked = true,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
                replyToAuthorHandle = null,
            ),
        )
}

@Preview
@Composable
fun PreviewFeedNoteListItemLightMultiLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunrise) {
        FeedNoteCard(
            data = feedPostUi,
            headerSingleLine = false,
            fullWidthContent = false,
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
            onMuteUserClick = {},
            onMediaClick = { _, _ -> },
            onReportContentClick = { _, _, _ -> },
            onBookmarkClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewFeedNoteListItemLightMultiLineHeaderFullWidth(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunrise) {
        FeedNoteCard(
            data = feedPostUi,
            headerSingleLine = false,
            fullWidthContent = true,
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
            onMuteUserClick = {},
            onMediaClick = { _, _ -> },
            onReportContentClick = { _, _, _ -> },
            onBookmarkClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewFeedNoteListItemDarkSingleLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        FeedNoteCard(
            data = feedPostUi,
            headerSingleLine = true,
            fullWidthContent = false,
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
            onMuteUserClick = {},
            onMediaClick = { _, _ -> },
            onReportContentClick = { _, _, _ -> },
            onBookmarkClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewFeedNoteListItemDarkSingleLineHeaderFullWidth(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        FeedNoteCard(
            data = feedPostUi,
            headerSingleLine = true,
            fullWidthContent = true,
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
            onMuteUserClick = {},
            onMediaClick = { _, _ -> },
            onReportContentClick = { _, _, _ -> },
            onBookmarkClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewFeedNoteListItemLightForcedContentIndentFullWidthSingleLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunrise) {
        FeedNoteCard(
            data = feedPostUi,
            headerSingleLine = true,
            fullWidthContent = true,
            forceContentIndent = true,
            drawLineBelowAvatar = true,
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
            onMuteUserClick = {},
            onMediaClick = { _, _ -> },
            onReportContentClick = { _, _, _ -> },
            onBookmarkClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewFeedNoteListItemDarkForcedContentIndentSingleLineHeader(
    @PreviewParameter(FeedPostUiProvider::class)
    feedPostUi: FeedPostUi,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        FeedNoteCard(
            data = feedPostUi,
            headerSingleLine = true,
            fullWidthContent = false,
            forceContentIndent = true,
            drawLineBelowAvatar = true,
            onPostClick = {},
            onProfileClick = {},
            onPostAction = {},
            onPostLongClickAction = {},
            onHashtagClick = {},
            onMuteUserClick = {},
            onMediaClick = { _, _ -> },
            onReportContentClick = { _, _, _ -> },
            onBookmarkClick = {},
        )
    }
}
