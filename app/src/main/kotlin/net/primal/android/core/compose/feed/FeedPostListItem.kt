package net.primal.android.core.compose.feed

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PostImageListItemImage
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.feed.model.FeedPostResource
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedLikes
import net.primal.android.core.compose.icons.primaliconpack.FeedLikesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedReplies
import net.primal.android.core.compose.icons.primaliconpack.FeedRepliesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.core.compose.icons.primaliconpack.FeedRepostsFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.FeedZapsFilled
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.asBeforeNowFormat
import net.primal.android.core.utils.isPrimalIdentifier
import net.primal.android.nostr.model.primal.PrimalResourceVariant
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun FeedPostListItem(
    data: FeedPostUi,
    shouldIndentContent: Boolean = false,
    connected: Boolean = false,
    highlighted: Boolean = false,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
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
        if (data.repostAuthorDisplayName != null) {
            RepostedItem(
                repostedBy = data.repostAuthorDisplayName,
                onRepostAuthorClick = {
                    if (data.repostAuthorId != null) {
                        onProfileClick(data.repostAuthorId)
                    }
                }
            )
        }

        PostAuthorItem(
            authorDisplayName = data.authorDisplayName,
            postTimestamp = data.timestamp,
            authorAvatarUrl = data.authorAvatarUrl,
            authorInternetIdentifier = data.authorInternetIdentifier,
            onAuthorAvatarClick = { onProfileClick(data.authorId) },
        )

        val postAuthorGuessHeight = with(LocalDensity.current) { 128.dp.toPx() }

        Column(
            modifier = Modifier.padding(start = if (shouldIndentContent) 64.dp else 0.dp),
        ) {
            PostContent(
                content = data.content,
                resources = data.resources,
                onClick = {
                    uiScope.launch {
                        val press = PressInteraction.Press(it.copy(y = it.y + postAuthorGuessHeight))
                        interactionSource.emit(press)
                        interactionSource.emit(PressInteraction.Release(press))
                    }
                    onPostClick(data.postId)
                },
                onUrlClick = {
                    localUriHandler.openUriSafely(it)
                },
            )

            PostStatsItem(
                postStats = data.stats,
            )
        }
    }
}

@Composable
private fun CardWithHighlight(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    highlightWidth: Dp = 8.dp,
    connected: Boolean = false,
    connectionWidth: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit,
) {

    val outlineColor = AppTheme.colorScheme.outline
    val gradientColors = listOf(
        AppTheme.extraColorScheme.brand1,
        AppTheme.extraColorScheme.brand2,
    )

    Card(
        modifier = modifier,
    ) {
        if (highlighted || connected) {
            Column(
                modifier = Modifier.drawWithCache {
                    onDrawBehind {
                        if (highlighted) {
                            drawLine(
                                brush = Brush.verticalGradient(gradientColors),
                                start = Offset(x = 0f, y = 0f),
                                end = Offset(x = 0f, y = size.height),
                                strokeWidth = highlightWidth.toPx(),
                                cap = StrokeCap.Square
                            )
                        }

                        val connectionX = 40.dp.toPx()

                        if (connected) {
                            drawLine(
                                color = outlineColor,
                                start = Offset(x = connectionX, y = 80.dp.toPx()),
                                end = Offset(x = connectionX, y = size.height - 16.dp.toPx()),
                                strokeWidth = connectionWidth.toPx(),
                                cap = StrokeCap.Square
                            )
                        }
                    }
                }
            ) {
                content()
            }
        } else {
            content()
        }
    }
}

private fun List<FeedPostResource>.filterImages() = filter {
    it.mimeType?.startsWith("image") == true
}

private fun List<FeedPostResource>.filterNotImages() = filterNot {
    it.mimeType?.startsWith("image") == true
}

private fun String.withoutUrls(urls: List<String>): String {
    var newContent = this
    urls.forEach {
        newContent = newContent.replace(it, "")
    }
    return newContent
}

@Composable
fun PostContent(
    content: String,
    resources: List<FeedPostResource>,
    onClick: (Offset) -> Unit,
    onUrlClick: (String) -> Unit,
) {
    val imageResources = remember { resources.filterImages() }
    val refinedUrlResources = remember { resources.filterNotImages() }

    val refinedContent = remember {
        content.withoutUrls(urls = imageResources.map { it.url }).trim()
    }

    val contentText = buildAnnotatedString {
        append(refinedContent)
        refinedUrlResources
            .map { it.url }
            .forEach {
                val startIndex = refinedContent.indexOf(it)
                val endIndex = startIndex + it.length
                addStyle(
                    style = SpanStyle(
                        color = AppTheme.colorScheme.primary,
                    ),
                    start = startIndex,
                    end = endIndex,
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = it,
                    start = startIndex,
                    end = endIndex,
                )
            }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        if (contentText.isNotEmpty()) {
            PrimalClickableText(
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colorScheme.onSurface
                ),
                text = contentText,
                maxLines = 12,
                overflow = TextOverflow.Ellipsis,
                onClick = { position, offset ->
                    contentText.getStringAnnotations(tag = "URL", start = position, end = position)
                        .firstOrNull()?.let { annotation ->
                            onUrlClick(annotation.item)
                        } ?: onClick(offset)
                }
            )
        }

        if (imageResources.isNotEmpty()) {
            when (imageResources.size) {
                1 -> {
                    BoxWithConstraints(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clip(AppTheme.shapes.medium),
                    ) {
                        val resource = imageResources.first()

                        val density = LocalDensity.current.density
                        val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
                        val maxWidth = maxWidth.value.toInt()
                        val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.77).toInt()

                        val variant = resource.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
                        val imageSizeDp = variant.calculateImageSize(
                            maxWidth = maxWidth,
                            maxHeight = maxHeight,
                            density = density
                        )
                        val imageSource = variant?.mediaUrl ?: resource.url
                        PostImageListItemImage(
                            source = imageSource,
                            modifier = Modifier
                                .width(imageSizeDp.width)
                                .height(imageSizeDp.height)
                        )
                    }
                }

                else -> {}
            }
        }
    }

}

private fun List<PrimalResourceVariant>.findNearestOrNull(maxWidthPx: Int): PrimalResourceVariant? {
    return sortedBy { it.width }.find { it.width >= maxWidthPx }
}

private fun PrimalResourceVariant?.calculateImageSize(
    maxWidth: Int,
    maxHeight: Int,
    density: Float
): DpSize {
    if (this == null) return DpSize(maxWidth.dp, maxWidth.dp)

    val variantWidth = (width / density).toInt()
    val variantHeight = (height / density).toInt()
    return DpSize(
        width = when {
            else -> maxWidth.dp
        },
        height = when {
            variantHeight == 0 -> maxWidth.dp
            variantHeight > maxHeight -> maxHeight.dp
            else -> ((maxWidth * variantHeight) / variantWidth).dp
        }
    )
}

private fun Int.toPostStatString(): String = if (this > 0) toString() else ""

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
            textCount = postStats.repliesCount.toPostStatString(),
            highlight = postStats.userReplied,
            iconVector = PrimalIcons.FeedReplies,
            iconVectorHighlight = PrimalIcons.FeedRepliesFilled,
        )

        SinglePostStat(
            textCount = postStats.satsZapped.toPostStatString(),
            highlight = postStats.userZapped,
            iconVector = PrimalIcons.FeedZaps,
            iconVectorHighlight = PrimalIcons.FeedZapsFilled,
        )

        SinglePostStat(
            textCount = postStats.likesCount.toPostStatString(),
            highlight = postStats.userLiked,
            iconVector = PrimalIcons.FeedLikes,
            iconVectorHighlight = PrimalIcons.FeedLikesFilled,
        )

        SinglePostStat(
            textCount = postStats.repostsCount.toPostStatString(),
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
                24.sp, 24.sp, PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = if (highlight) iconVectorHighlight else iconVector,
                    contentDescription = null,
                    colorFilter = if (!highlight) {
                        ColorFilter.tint(color = AppTheme.extraColorScheme.onSurfaceVariantAlt4)
                    } else null
                )
            }
        }
    )

    Text(
        text = titleText,
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
        inlineContent = inlineContent,
    )
}

@Composable
fun PostAuthorItem(
    authorDisplayName: String,
    postTimestamp: Instant,
    authorAvatarUrl: String? = null,
    authorInternetIdentifier: String? = null,
    onAuthorAvatarClick: () -> Unit,
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
            source = authorAvatarUrl,
            hasBorder = authorInternetIdentifier.isPrimalIdentifier(),
            modifier = Modifier.clickable { onAuthorAvatarClick() },
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {

            val timestamp = postTimestamp.asBeforeNowFormat(
                res = LocalContext.current.resources
            )

            val suffixText = buildAnnotatedString {
                if (!hasVerifiedBadge) append(' ')
                append(
                    AnnotatedString(
                        text = "| $timestamp",
                        spanStyle = SpanStyle(
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            fontStyle = AppTheme.typography.bodyMedium.fontStyle,
                        )
                    )
                )
            }
            NostrUserText(
                displayName = authorDisplayName,
                internetIdentifier = authorInternetIdentifier,
                annotatedStringSuffixBuilder = {
                    append(suffixText)
                }
            )

            if (!authorInternetIdentifier.isNullOrEmpty()) {
                Text(
                    text = authorInternetIdentifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
    }
}

@Composable
private fun RepostedItem(
    repostedBy: String,
    onRepostAuthorClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {

        PrimalClickableText(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.TopStart),
            text = buildAnnotatedString {
                appendInlineContent("icon", "[icon]")
                append(' ')
                append(
                    AnnotatedString(
                        text = repostedBy,
                        spanStyle = SpanStyle(
                            color = AppTheme.colorScheme.primary,
                        )
                    )
                )
                append(' ')
                append(
                    AnnotatedString(
                        text = stringResource(id = R.string.feed_reposted_suffix),
                        spanStyle = SpanStyle(
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        )
                    )
                )
                append(' ')
            },
            style = AppTheme.typography.bodyMedium,
            onClick = { _, _ ->
                onRepostAuthorClick()
            },
            inlineContent = mapOf(
                "icon" to InlineTextContent(
                    placeholder = Placeholder(24.sp, 24.sp, PlaceholderVerticalAlign.TextCenter)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = PrimalIcons.FeedReposts,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2
                            ),
                        )
                    }
                }
            )
        )
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
                repostAuthorDisplayName = "jack",
                content = """
                    Unfortunately the days of using pseudonyms in metaspace are numbered. 

                    It won't be long before non-trivial numbers of individuals and businesses 
                    have augmented reality HUDs that incorporate real-time facial recognition. 
                    Hiding behind a pseudonym will become a distant dream.
                """.trimIndent(),
                resources = emptyList(),
                authorId = "npubSomething",
                authorDisplayName = "android_robots_from_space",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarUrl = "https://i.imgur.com/Z8dpmvc.png",
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                stats = FeedPostStatsUi(
                    repliesCount = 11,
                    likesCount = 256,
                    userLiked = true,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
            ),
            onPostClick = {},
            onProfileClick = {},
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
                repostAuthorDisplayName = "jack",
                content = """
                    Unfortunately the days of using pseudonyms in metaspace are numbered. 

                    It won't be long before non-trivial numbers of individuals and businesses 
                    have augmented reality HUDs that incorporate real-time facial recognition. 
                    Hiding behind a pseudonym will become a distant dream.
                """.trimIndent(),
                resources = emptyList(),
                authorId = "npubSomething",
                authorDisplayName = "android",
                authorInternetIdentifier = "android@primal.net",
                authorAvatarUrl = "https://i.imgur.com/Z8dpmvc.png",
                timestamp = Instant.now().minus(30, ChronoUnit.MINUTES),
                stats = FeedPostStatsUi(
                    repliesCount = 11,
                    userReplied = true,
                    likesCount = 256,
                    repostsCount = 42,
                    satsZapped = 555,
                ),
            ),
            onPostClick = {},
            onProfileClick = {},
        )
    }

}
