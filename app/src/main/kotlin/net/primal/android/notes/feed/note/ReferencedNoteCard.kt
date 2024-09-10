package net.primal.android.notes.feed.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.minutes
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.toNoteContentUi
import net.primal.android.notes.feed.note.events.InvoicePayClickEvent
import net.primal.android.notes.feed.note.events.MediaClickEvent
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun ReferencedNoteCard(
    modifier: Modifier = Modifier,
    data: FeedPostUi,
    onPostClick: ((String) -> Unit)? = null,
    onProfileClick: ((profileId: String) -> Unit)? = null,
    onArticleClick: ((naddr: String) -> Unit)? = null,
    onMediaClick: ((MediaClickEvent) -> Unit)? = null,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    ),
) {
    NoteSurfaceCard(
        modifier = modifier
            .wrapContentHeight()
            .clickable(
                enabled = onPostClick != null,
                onClick = { onPostClick?.invoke(data.postId) },
            ),
        colors = colors,
        border = BorderStroke(width = 0.5.dp, color = AppTheme.colorScheme.outline),
    ) {
        FeedNoteHeader(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp),
            authorDisplayName = data.authorName,
            postTimestamp = data.timestamp,
            singleLine = true,
            authorAvatarSize = 30.dp,
            authorAvatarCdnImage = data.authorAvatarCdnImage,
            authorInternetIdentifier = data.authorInternetIdentifier,
            onAuthorAvatarClick = { onProfileClick?.invoke(data.authorId) },
        )

        NoteContent(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 4.dp),
            data = data.toNoteContentUi(),
            expanded = false,
            onClick = { onPostClick?.invoke(data.postId) },
            onProfileClick = { onPostClick?.invoke(data.postId) },
            onPostClick = { postId -> onPostClick?.invoke(postId) },
            onArticleClick = onArticleClick,
            onUrlClick = { onPostClick?.invoke(data.postId) },
            onHashtagClick = { onPostClick?.invoke(data.postId) },
            onMediaClick = onMediaClick,
            onPayInvoiceClick = onPayInvoiceClick,
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview
@Composable
fun PreviewReferencedPostListItemLight() {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        ReferencedNoteCard(
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
                attachments = emptyList(),
                authorId = "npubSomething",
                authorName = "android_robots_from_space",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(30.minutes.inWholeMinutes, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 11,
                    likesCount = 256,
                    userLiked = true,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
                replyToAuthorHandle = "alex",
            ),
            onPostClick = {},
            onArticleClick = {},
            onMediaClick = {},
        )
    }
}

@Preview
@Composable
fun PreviewReferencedPostListItemDark() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ReferencedNoteCard(
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
                attachments = emptyList(),
                authorId = "npubSomething",
                authorName = "android",
                authorHandle = "user",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                timestamp = Instant.now().minus(30.minutes.inWholeMinutes, ChronoUnit.MINUTES),
                nostrUris = emptyList(),
                stats = EventStatsUi(
                    repliesCount = 11,
                    userReplied = true,
                    likesCount = 256,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
                hashtags = listOf("#nostr"),
                rawNostrEventJson = "",
                replyToAuthorHandle = null,
            ),
            onPostClick = {},
            onArticleClick = {},
            onMediaClick = {},
        )
    }
}
