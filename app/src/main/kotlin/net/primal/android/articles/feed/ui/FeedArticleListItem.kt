package net.primal.android.articles.feed.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import coil.compose.SubcomposeAsyncImage
import java.time.Instant
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.feed.model.EventStatsUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedReplies
import net.primal.android.core.compose.icons.primaliconpack.LightningBolt
import net.primal.android.note.ui.EventZapUiModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.user.domain.ContentDisplaySettings

@Composable
fun FeedArticleListItem(
    data: FeedArticleUi,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.clickable(
            enabled = onClick != null,
            onClick = { onClick?.invoke() },
        ),
    ) {
        val infoTextStyle = AppTheme.typography.bodyMedium.copy(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            fontSize = 15.sp,
            lineHeight = 15.sp,
        )
        Column(modifier = modifier) {
            ListItemHeader(data = data, textStyle = infoTextStyle)

            ListItemContent(data = data)

            ListItemFooter(data = data, textStyle = infoTextStyle)
        }
    }
}

@Composable
private fun ListItemHeader(data: FeedArticleUi, textStyle: TextStyle) {
    Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnail(
            avatarSize = 24.dp,
            borderSize = 0.dp,
            avatarCdnImage = data.authorAvatarCdnImage,
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp)
                .weight(1f),
            text = "${data.authorName} • ${data.publishedAt.asBeforeNowFormat(shortFormat = false)}",
            style = textStyle,
        )

        ArticleDropdownMenuIcon(
            modifier = Modifier
                .size(42.dp)
                .padding(top = 6.dp)
                .clip(CircleShape),
            articleId = data.articleId,
            noteContent = data.content,
            noteRawData = data.rawNostrEventJson,
            authorId = data.authorId,
            isBookmarked = data.isBookmarked,
            onBookmarkClick = { },
            onMuteUserClick = { },
            onReportContentClick = { },
        )
    }
}

@Composable
private fun ListItemContent(data: FeedArticleUi) {
    Row(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            modifier = Modifier
                .padding(top = 8.dp, end = 16.dp)
                .heightIn(min = imageHeightMin)
                .weight(1f),
            text = data.title,
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colorScheme.onPrimary,
            ),
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )

        val (imageSource, imageSize) = data.resolveImageUrlAndImageSize()
        SubcomposeAsyncImage(
            model = imageSource,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(imageSize)
                .border(
                    width = 0.5.dp,
                    color = AppTheme.colorScheme.outlineVariant,
                    shape = AppTheme.shapes.extraSmall,
                )
                .clip(AppTheme.shapes.extraSmall),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            loading = { ArticleImagePlaceholder() },
            error = { ArticleImagePlaceholder() },
        )
    }
}

@Composable
private fun ListItemFooter(data: FeedArticleUi, textStyle: TextStyle) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (data.readingTimeInMinutes != null) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.article_feed_reading_time, data.readingTimeInMinutes),
                    style = textStyle,
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            val commentsCount = data.stats.repliesCount.toInt()
            IconText(
                modifier = Modifier,
                text = pluralStringResource(
                    id = R.plurals.article_feed_comments_count,
                    count = commentsCount,
                    commentsCount,
                ),
                leadingIcon = PrimalIcons.FeedReplies,
                style = textStyle,
                color = textStyle.color,
            )
        }

        if (data.eventZaps.isNotEmpty()) {
            val zaps = data.eventZaps.take(n = 3).reversed()
            Row(
                modifier = Modifier.wrapContentWidth(align = Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = PrimalIcons.LightningBolt,
                    contentDescription = null,
                    tint = textStyle.color,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    repeat(times = zaps.size) { index ->
                        AvatarThumbnail(
                            modifier = Modifier.padding(end = index.times(18.dp)),
                            avatarSize = 24.dp,
                            avatarCdnImage = zaps[index].zapperAvatarCdnImage,
                            hasBorder = true,
                            borderSize = 1.dp,
                            borderColor = AppTheme.colorScheme.surface,
                        )
                    }
                }
            }
        }
    }
}

private val imageWidth = 100.dp
private val imageHeightMax = 144.dp
private val imageHeightMin = 36.dp

@Composable
private fun FeedArticleUi.resolveImageUrlAndImageSize(): Pair<String?, DpSize> {
    val density = LocalDensity.current
    val maxWidthPx = with(density) { imageWidth.roundToPx() }
    val variant = this.imageCdnImage?.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    val imageSource = variant?.mediaUrl ?: this.imageCdnImage?.sourceUrl

    val imageSize = if (variant == null) {
        DpSize(width = imageWidth, height = imageWidth)
    } else {
        val imageHeightPx = (variant.height / variant.width) * maxWidthPx
        val imageHeight = with(density) { imageHeightPx.toDp() }
        DpSize(
            width = imageWidth,
            height = imageHeight.coerceIn(
                minimumValue = imageHeightMin,
                maximumValue = imageHeightMax,
            ),
        )
    }
    return Pair(imageSource, imageSize)
}

@Composable
private fun ArticleImagePlaceholder() {
    Box(contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = Color.Black,
        )
    }
}

@Preview
@Composable
private fun PreviewFeedArticleListItem() {
    CompositionLocalProvider(
        LocalContentDisplaySettings provides ContentDisplaySettings(),
    ) {
        PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
            FeedArticleListItem(
                data = FeedArticleUi(
                    eventId = "",
                    articleId = "",
                    authorId = "1234",
                    authorName = "JeffG",
                    content = "This is content.",
                    title = "Purple Tech",
                    isBookmarked = false,
                    publishedAt = Instant.now(),
                    rawNostrEventJson = "raaaaw",
                    readingTimeInMinutes = 5,
                    stats = EventStatsUi(
                        repliesCount = 23,
                    ),
                    eventZaps = listOf(
                        EventZapUiModel(
                            id = "",
                            zapperId = "",
                            zapperName = "",
                            zapperHandle = "",
                            amountInSats = 200L.toULong(),
                            message = "",
                            zappedAt = 0,
                        ),
                        EventZapUiModel(
                            id = "",
                            zapperId = "",
                            zapperName = "",
                            zapperHandle = "",
                            amountInSats = 200L.toULong(),
                            message = "",
                            zappedAt = 0,
                        ),
                        EventZapUiModel(
                            id = "",
                            zapperId = "",
                            zapperName = "",
                            zapperHandle = "",
                            amountInSats = 200L.toULong(),
                            message = "",
                            zappedAt = 0,
                        ),
                    ),
                ),
                modifier = Modifier.padding(all = 16.dp),
            )
        }
    }
}
