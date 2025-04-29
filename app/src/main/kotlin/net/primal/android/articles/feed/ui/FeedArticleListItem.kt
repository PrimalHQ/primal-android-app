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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.WrappedContentWithSuffix
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedReplies
import net.primal.android.core.compose.icons.primaliconpack.LightningBolt
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.zaps.ZappersAvatarThumbnailRow
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.findNearestOrNull
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.ReportType

@ExperimentalMaterial3Api
@Composable
fun FeedArticleListItem(
    data: FeedArticleUi,
    modifier: Modifier = Modifier,
    enabledDropdownMenu: Boolean = true,
    showCommentsCount: Boolean = true,
    isArticleAuthor: Boolean = false,
    color: Color = AppTheme.colorScheme.surfaceVariant,
    onClick: ((naddr: String) -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    onMuteUserClick: (() -> Unit)? = null,
    onReportContentClick: ((reportType: ReportType) -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.clickable(
            enabled = onClick != null,
            onClick = {
                val naddr = Naddr(
                    identifier = data.articleId,
                    userId = data.authorId,
                    kind = NostrEventKind.LongFormContent.value,
                ).toNaddrString()
                onClick?.invoke(naddr)
            },
        ),
        color = color,
    ) {
        val infoTextStyle = AppTheme.typography.bodyMedium.copy(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            fontSize = 15.sp,
            lineHeight = 15.sp,
        )
        Column(modifier = modifier) {
            ListItemHeader(
                data = data,
                textStyle = infoTextStyle,
                isArticleAuthor = isArticleAuthor,
                enabledDropdownMenu = enabledDropdownMenu,
                onBookmarkClick = onBookmarkClick,
                onMuteUserClick = onMuteUserClick,
                onReportContentClick = onReportContentClick,
            )

            ListItemContent(data = data)

            ListItemFooter(
                data = data,
                textStyle = infoTextStyle,
                showCommentsCount = showCommentsCount,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ListItemHeader(
    data: FeedArticleUi,
    isArticleAuthor: Boolean,
    textStyle: TextStyle,
    enabledDropdownMenu: Boolean = true,
    onBookmarkClick: (() -> Unit)? = null,
    onMuteUserClick: (() -> Unit)? = null,
    onReportContentClick: ((reportType: ReportType) -> Unit)? = null,
) {
    val overflowIconSizeDp = 40.dp
    Box(contentAlignment = Alignment.TopEnd) {
        if (enabledDropdownMenu) {
            ArticleDropdownMenuIcon(
                modifier = Modifier
                    .size(overflowIconSizeDp)
                    .padding(top = 6.dp)
                    .offset(x = 4.dp)
                    .clip(CircleShape),
                articleId = data.articleId,
                articleContent = data.content,
                articleRawData = data.rawNostrEventJson,
                authorId = data.authorId,
                isBookmarked = data.isBookmarked,
                onBookmarkClick = onBookmarkClick,
                onMuteUserClick = onMuteUserClick,
                onReportContentClick = onReportContentClick,
                isArticleAuthor = isArticleAuthor,
                icon = {
                    Icon(
                        modifier = Modifier
                            .padding(start = 14.dp, end = 8.dp)
                            .wrapContentSize(align = Alignment.TopEnd),
                        imageVector = PrimalIcons.More,
                        contentDescription = stringResource(id = R.string.accessibility_article_drop_down),
                    )
                },
            )
        }

        Row(
            modifier = Modifier
                .height(32.dp)
                .padding(end = overflowIconSizeDp - 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UniversalAvatarThumbnail(
                avatarSize = 24.dp,
                avatarCdnImage = data.authorAvatarCdnImage,
                avatarBlossoms = data.authorBlossoms,
                legendaryCustomization = data.authorLegendaryCustomization,
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                WrappedContentWithSuffix(
                    modifier = Modifier,
                    wrappedContent = {
                        Text(
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            text = data.authorName,
                            style = textStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    suffixFixedContent = {
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = " • ${data.publishedAt.asBeforeNowFormat()}",
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            style = textStyle,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ListItemContent(data: FeedArticleUi, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(top = 8.dp, end = 16.dp)
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

        val imageSource = data.resolveImageUrl()
        SubcomposeAsyncImage(
            model = imageSource,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(width = imageWidth, height = imageHeight)
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
private fun ListItemFooter(
    data: FeedArticleUi,
    textStyle: TextStyle,
    showCommentsCount: Boolean = true,
) {
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

            if (showCommentsCount) {
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
                ZappersAvatarThumbnailRow(zaps = zaps)
            }
        }
    }
}

private val imageWidth = 100.dp
private val imageHeight = 72.dp

@Composable
private fun FeedArticleUi.resolveImageUrl(): String? {
    val density = LocalDensity.current
    val maxWidthPx = with(density) { imageWidth.roundToPx() }
    val variant = this.imageCdnImage?.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    val imageSource = variant?.mediaUrl ?: this.imageCdnImage?.sourceUrl
    return imageSource
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

private class FeedArticleUiProvider : PreviewParameterProvider<FeedArticleUi> {
    override val values: Sequence<FeedArticleUi>
        get() = sequenceOf(
            FeedArticleUi(
                aTag = "",
                eventId = "",
                articleId = "",
                authorId = "1234",
                authorName = "majstor",
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
            FeedArticleUi(
                aTag = "",
                eventId = "",
                articleId = "",
                authorId = "1234",
                authorName = "majstor of long author user names and other stuff",
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
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewFeedArticleListItem(
    @PreviewParameter(provider = FeedArticleUiProvider::class)
    articleUi: FeedArticleUi,
) {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        FeedArticleListItem(
            data = articleUi,
            modifier = Modifier.padding(all = 16.dp),
        )
    }
}
