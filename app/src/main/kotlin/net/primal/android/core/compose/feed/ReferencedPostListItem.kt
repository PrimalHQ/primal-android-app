package net.primal.android.core.compose.feed

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun ReferencedPostListItem(
    data: FeedPostUi,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .clickable {
                onPostClick(data.postId)
            },
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt,
        ),
    ) {

        ReferencedPostAuthorRow(
            authorDisplayName = data.authorName,
            postTimestamp = data.timestamp,
            authorAvatarUrl = data.authorAvatarUrl,
            authorResources = data.authorMediaResources,
            authorInternetIdentifier = data.authorInternetIdentifier,
        )

        FeedPostContent(
            content = data.content.trim(),
            expanded = false,
            hashtags = data.hashtags,
            mediaResources = data.mediaResources,
            nostrResources = data.nostrResources,
            onClick = { onPostClick(data.postId) },
            onProfileClick = { onPostClick(data.postId) },
            onPostClick = { postId -> onPostClick(postId) },
            onUrlClick = { onPostClick(data.postId) },
            onHashtagClick = { onPostClick(data.postId) },
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun PreviewReferencedPostListItemLight() {
    PrimalTheme {
        ReferencedPostListItem(
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
        )
    }

}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewReferencedPostListItemDark() {
    PrimalTheme {
        ReferencedPostListItem(
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
        )
    }
}
