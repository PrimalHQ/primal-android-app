package net.primal.android.core.compose.feed

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.ext.openUriSafely
import net.primal.android.theme.PrimalTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun FeedPostListItem(
    data: FeedPostUi,
    shouldIndentContent: Boolean = false,
    connected: Boolean = false,
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
            .padding(horizontal = 4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { onPostClick(data.postId) },
            ),
        highlighted = highlighted,
        connected = connected,
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

        FeedPostAuthorRow(
            authorDisplayName = data.authorName,
            postTimestamp = data.timestamp,
            authorAvatarUrl = data.authorAvatarUrl,
            authorResources = data.authorMediaResources,
            authorInternetIdentifier = data.authorInternetIdentifier,
            onAuthorAvatarClick = { onProfileClick(data.authorId) },
        )

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
                postStats = data.stats,
                onPostAction = onPostAction,
                onPostLongPressAction = onPostLongClickAction,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun PreviewFeedPostListItemLight() {
    PrimalTheme {
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewFeedPostListItemDark() {
    PrimalTheme {
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
