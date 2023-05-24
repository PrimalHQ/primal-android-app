package net.primal.android.feed.ui.post

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedLikes
import net.primal.android.core.compose.icons.primaliconpack.FeedLikesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedReplies
import net.primal.android.core.compose.icons.primaliconpack.FeedRepliesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.core.compose.icons.primaliconpack.FeedRepostsFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.FeedZapsFilled
import net.primal.android.core.compose.icons.primaliconpack.Verified
import net.primal.android.core.utils.asBeforeNowFormat
import net.primal.android.feed.ui.FeedPostStatsUi
import net.primal.android.feed.ui.FeedPostUi
import net.primal.android.theme.PrimalTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun FeedPostListItem(
    data: FeedPostUi,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(

        ),
    ) {
        if (data.repostAuthorDisplayName != null) {
            RepostedItem(repostedBy = data.repostAuthorDisplayName)
        }

        PostAuthorItem(
            authorDisplayName = data.authorDisplayName,
            postTimestamp = data.timestamp,
            authorAvatarUrl = data.authorAvatarUrl,
            authorInternetIdentifier = data.authorInternetIdentifier,
        )

        PostContent(
            content = data.content,
        )

        PostStatsItem(
            postStats = data.stats,
        )
    }
}

@Composable
fun PostContent(
    content: String,
) {
    val contentText = buildAnnotatedString {
        append(content)
    }

    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = contentText,
        maxLines = 12,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun PostStatsItem(
    postStats: FeedPostStatsUi,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SinglePostStat(
            textCount = postStats.repliesCount.toString(),
            highlight = postStats.userReplied,
            iconVector = PrimalIcons.FeedReplies,
            iconVectorHighlight = PrimalIcons.FeedRepliesFilled,
        )

        SinglePostStat(
            textCount = postStats.zapsCount.toString(),
            highlight = postStats.userZapped,
            iconVector = PrimalIcons.FeedZaps,
            iconVectorHighlight = PrimalIcons.FeedZapsFilled,
        )

        SinglePostStat(
            textCount = postStats.likesCount.toString(),
            highlight = postStats.userLiked,
            iconVector = PrimalIcons.FeedLikes,
            iconVectorHighlight = PrimalIcons.FeedLikesFilled,
        )

        SinglePostStat(
            textCount = postStats.repostsCount.toString(),
            highlight = postStats.userReposted,
            iconVector = PrimalIcons.FeedReposts,
            iconVectorHighlight = PrimalIcons.FeedRepostsFilled,
        )
    }
}

@Composable
fun SinglePostStat(
    textCount: String,
    highlight: Boolean,
    iconVector: ImageVector,
    iconVectorHighlight: ImageVector,
) {
    val titleText = buildAnnotatedString {
        appendInlineContent("icon", "[icon]")
        append(' ')
        append(textCount)
    }

    val inlineContent = mapOf(
        "icon" to InlineTextContent(
            placeholder = Placeholder(
                24.sp, 24.sp, PlaceholderVerticalAlign.Center
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = if (highlight) iconVectorHighlight else iconVector,
                    contentDescription = null,
                    colorFilter = if (highlight) ColorFilter.tint(
                        color = Color(0xFFAB268E)
                    ) else null
                )
            }
        }
    )

    Text(
        text = titleText,
        inlineContent = inlineContent,
    )
}

@Composable
fun PostAuthorItem(
    authorDisplayName: String,
    postTimestamp: Instant,
    authorAvatarUrl: String? = null,
    authorInternetIdentifier: String? = null,
) {

    val hasVerifiedBadge = !authorInternetIdentifier.isNullOrEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnailListItemImage(
            source = authorAvatarUrl
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {

            val titleText = buildAnnotatedString {
                append(authorDisplayName)
                if (hasVerifiedBadge) {
                    appendInlineContent("verifiedBadge", "[badge]")
                }
                append(" | ")
                append(postTimestamp.asBeforeNowFormat(res = LocalContext.current.resources))
            }

            val inlineContent = mapOf(
                "verifiedBadge" to InlineTextContent(
                    placeholder = Placeholder(
                        24.sp, 24.sp, PlaceholderVerticalAlign.Center
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = PrimalIcons.Verified,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color = if (authorInternetIdentifier?.contains("primal.net") == true) {
                                    Color(0xFFAB268E)
                                } else {
                                    Color(0xFF666666)
                                }
                            )
                        )
                    }
                }
            )

            Text(
                text = titleText,
                inlineContent = inlineContent,
            )

            if (authorInternetIdentifier?.isNotEmpty() == true) {
                Text(
                    text = authorInternetIdentifier.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RepostedItem(
    repostedBy: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        PrimalClickableText(
            modifier = Modifier.fillMaxWidth(),
            text = buildAnnotatedString {
                appendInlineContent("icon", "[icon]")
                append(' ')
                append(
                    AnnotatedString(
                        text = repostedBy,
                        spanStyle = SpanStyle(
                            color = PrimalTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                )
                append(' ')
                append(AnnotatedString(stringResource(id = R.string.feed_reposted_suffix)))
            },
            style = PrimalTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Start,
                lineHeight = 16.sp,
            ),
            onClick = {

            },
            inlineContent = mapOf(
                "icon" to InlineTextContent(
                    placeholder = Placeholder(24.sp, 24.sp, PlaceholderVerticalAlign.Center)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = PrimalIcons.FeedReposts,
                            contentDescription = null
                        )
                    }
                }
            )
        )
    }
}

@Preview
@Composable
fun PreviewFeedPostListItem() {
    PrimalTheme {
        FeedPostListItem(
            data = FeedPostUi(
                postId = "random",
                repostId = "repostRandom",
                content = "My content.",
                authorDisplayName = "miljan",
                authorInternetIdentifier = "miljan@primal.net",
                authorAvatarUrl = "https://i.imgur.com/Z8dpmvc.png",
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                stats = FeedPostStatsUi(),
            ),
            onClick = {},
        )
    }

}
