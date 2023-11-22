package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.toNoteContentUi
import net.primal.android.core.ext.openUriSafely
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun FeedNoteCard(
    data: FeedPostUi,
    shape: Shape = CardDefaults.shape,
    cardPadding: PaddingValues = PaddingValues(all = 0.dp),
    fullWidthNote: Boolean = false,
    headerSingleLine: Boolean = true,
    fullWidthContent: Boolean = false,
    forceContentIndent: Boolean = false,
    drawLineAboveAvatar: Boolean = false,
    drawLineBelowAvatar: Boolean = false,
    expanded: Boolean = false,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostAction: (FeedPostAction) -> Unit,
    onPostLongClickAction: (FeedPostAction) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onMuteUserClick: () -> Unit,
) {
    val localUriHandler = LocalUriHandler.current
    val uiScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    val notePaddingDp = 4.dp
    val avatarPaddingDp = 8.dp
    val avatarSizeDp = if (fullWidthNote) 30.dp else 42.dp
    val overflowIconSizeDp = 40.dp

    NoteSurfaceCard(
        modifier = Modifier
            .wrapContentHeight()
            .padding(cardPadding)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { onPostClick(data.postId) },
            ),
        shape = shape,
        drawLineAboveAvatar = drawLineAboveAvatar,
        drawLineBelowAvatar = drawLineBelowAvatar,
        lineOffsetX = (avatarSizeDp / 2) + avatarPaddingDp + notePaddingDp,
    ) {
        Box(
            modifier = Modifier.padding(all = notePaddingDp),
            contentAlignment = Alignment.TopEnd,
        ) {
            NoteDropdownMenuIcon(
                modifier = Modifier
                    .size(overflowIconSizeDp)
                    .padding(all = 8.dp)
                    .clip(CircleShape),
                noteId = data.postId,
                noteContent = data.content,
                noteRawData = data.rawNostrEventJson,
                authorId = data.authorId,
                onMuteUserClick = onMuteUserClick,
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

                Row {
                    if (!fullWidthContent) {
                        AvatarThumbnail(
                            modifier = Modifier.padding(
                                start = avatarPaddingDp,
                                top = avatarPaddingDp,
                            ),
                            avatarSize = avatarSizeDp,
                            avatarCdnImage = data.authorAvatarCdnImage,
                            onClick = { onProfileClick(data.authorId) },
                        )
                    }

                    Column(
                        modifier = Modifier.padding(
                            start = 0.dp,
                        ),
                    ) {
                        FeedNoteHeader(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = avatarPaddingDp)
                                .padding(end = overflowIconSizeDp - 8.dp)
                                .fillMaxWidth(),
                            postTimestamp = data.timestamp,
                            singleLine = headerSingleLine,
                            authorAvatarVisible = fullWidthContent,
                            authorAvatarSize = avatarSizeDp,
                            authorDisplayName = data.authorName,
                            authorAvatarCdnImage = data.authorAvatarCdnImage,
                            authorInternetIdentifier = data.authorInternetIdentifier,
                            onAuthorAvatarClick = { onProfileClick(data.authorId) },
                        )

                        val postAuthorGuessHeight = with(LocalDensity.current) { 128.dp.toPx() }
                        val launchRippleEffect: (Offset) -> Unit = {
                            uiScope.launch {
                                val press =
                                    PressInteraction.Press(
                                        it.copy(y = it.y + postAuthorGuessHeight),
                                    )
                                interactionSource.emit(press)
                                interactionSource.emit(PressInteraction.Release(press))
                            }
                        }

                        NoteContent(
                            modifier = Modifier
                                .padding(horizontal = if (fullWidthContent) 10.dp else 8.dp)
                                .padding(
                                    start = if (forceContentIndent && fullWidthContent) {
                                        avatarSizeDp + 6.dp
                                    } else {
                                        0.dp
                                    },
                                )
                                .padding(
                                    top = if (fullWidthContent || !headerSingleLine) {
                                        10.dp
                                    } else {
                                        2.dp
                                    },
                                ),
                            data = data.toNoteContentUi(),
                            expanded = expanded,
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
                            onMediaClick = onMediaClick,
                        )

                        FeedNoteStatsRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(top = 8.dp)
                                .padding(bottom = 8.dp),
                            postStats = data.stats,
                            onPostAction = onPostAction,
                            onPostLongPressAction = onPostLongClickAction,
                        )
                    }
                }
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
        )
    }
}
