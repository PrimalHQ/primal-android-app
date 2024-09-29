package net.primal.android.notes.feed.note.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedBookmark
import net.primal.android.core.compose.icons.primaliconpack.FeedBookmarkFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewLike
import net.primal.android.core.compose.icons.primaliconpack.FeedNewLikeFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewReply
import net.primal.android.core.compose.icons.primaliconpack.FeedNewReplyFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewReposts
import net.primal.android.core.compose.icons.primaliconpack.FeedNewRepostsFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZap
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZapFilled
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostAction
import net.primal.android.theme.AppTheme

@Composable
fun FeedNoteActionsRow(
    modifier: Modifier,
    eventStats: EventStatsUi,
    isBookmarked: Boolean,
    highlightedNote: Boolean = false,
    showBookmark: Boolean = false,
    showCounts: Boolean = true,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongPressAction: ((FeedPostAction) -> Unit)? = null,
) {
    val iconSize = if (highlightedNote) 26.sp else 22.sp
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SingleEventStat(
            textCount = if (showCounts) eventStats.repliesCount.toPostStatString(numberFormat) else "",
            highlighted = eventStats.userReplied,
            iconSize = iconSize,
            iconVector = PrimalIcons.FeedNewReply,
            iconVectorHighlight = PrimalIcons.FeedNewReplyFilled,
            colorHighlight = AppTheme.extraColorScheme.replied,
            onClick = onPostAction?.let {
                { onPostAction(FeedPostAction.Reply) }
            },
            onLongClick = onPostLongPressAction?.let {
                { onPostLongPressAction(FeedPostAction.Reply) }
            },
            iconContentDescription = stringResource(id = R.string.accessibility_replies_count),
        )

        SingleEventStat(
            textCount = if (showCounts) eventStats.satsZapped.toPostStatString(numberFormat) else "",
            highlighted = eventStats.userZapped,
            iconVector = PrimalIcons.FeedNewZap,
            iconSize = iconSize,
            iconVectorHighlight = PrimalIcons.FeedNewZapFilled,
            colorHighlight = AppTheme.extraColorScheme.zapped,
            onClick = onPostAction?.let {
                { onPostAction(FeedPostAction.Zap) }
            },
            onLongClick = onPostLongPressAction?.let {
                { onPostLongPressAction(FeedPostAction.Zap) }
            },
            iconContentDescription = stringResource(id = R.string.accessibility_zaps_count),
        )

        SingleEventStat(
            textCount = if (showCounts) eventStats.likesCount.toPostStatString(numberFormat) else "",
            highlighted = eventStats.userLiked,
            iconSize = iconSize,
            iconVector = PrimalIcons.FeedNewLike,
            iconVectorHighlight = PrimalIcons.FeedNewLikeFilled,
            colorHighlight = AppTheme.extraColorScheme.liked,
            onClick = if (!eventStats.userLiked && onPostAction != null) {
                { onPostAction(FeedPostAction.Like) }
            } else {
                null
            },
            onLongClick = onPostLongPressAction?.let {
                { onPostLongPressAction(FeedPostAction.Like) }
            },
            iconContentDescription = stringResource(id = R.string.accessibility_likes_count),
        )

        SingleEventStat(
            textCount = if (showCounts) eventStats.repostsCount.toPostStatString(numberFormat) else "",
            highlighted = eventStats.userReposted,
            iconSize = iconSize,
            iconVector = PrimalIcons.FeedNewReposts,
            iconVectorHighlight = PrimalIcons.FeedNewRepostsFilled,
            colorHighlight = AppTheme.extraColorScheme.reposted,
            onClick = onPostAction?.let {
                { onPostAction(FeedPostAction.Repost) }
            },
            onLongClick = onPostLongPressAction?.let {
                { onPostLongPressAction(FeedPostAction.Repost) }
            },
            iconContentDescription = stringResource(id = R.string.accessibility_repost_count),
        )

        if (showBookmark) {
            SingleEventStat(
                textCount = "",
                highlighted = isBookmarked,
                iconSize = iconSize,
                iconVector = PrimalIcons.FeedBookmark,
                iconVectorHighlight = PrimalIcons.FeedBookmarkFilled,
                colorHighlight = AppTheme.extraColorScheme.bookmarked,
                onClick = onPostAction?.let {
                    { onPostAction(FeedPostAction.Bookmark) }
                },
                onLongClick = onPostLongPressAction?.let {
                    { onPostLongPressAction(FeedPostAction.Bookmark) }
                },
                iconContentDescription = stringResource(id = R.string.accessibility_bookmark),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleEventStat(
    textCount: String,
    highlighted: Boolean,
    iconVector: ImageVector,
    iconVectorHighlight: ImageVector,
    colorHighlight: Color,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    iconContentDescription: String? = null,
    iconSize: TextUnit = 24.sp,
    textStyle: TextStyle = AppTheme.typography.bodyMedium,
) {
    val titleText = buildAnnotatedString {
        appendInlineContent("icon", "[icon]")
        append(' ')
        append(textCount)
    }

    val inlineContent = mapOf(
        "icon" to InlineTextContent(
            placeholder = Placeholder(
                iconSize, iconSize, PlaceholderVerticalAlign.TextCenter,
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = if (!highlighted) iconVector else iconVectorHighlight,
                    contentDescription = iconContentDescription,
                    colorFilter = if (!highlighted) {
                        ColorFilter.tint(color = AppTheme.extraColorScheme.onSurfaceVariantAlt4)
                    } else {
                        null
                    },
                )
            }
        },
    )

    Text(
        modifier = Modifier
            .clip(CircleShape)
            .animateContentSize()
            .combinedClickable(
                enabled = onClick != null || onLongClick != null,
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick,
            ),
        text = titleText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = textStyle,
        color = if (!highlighted) AppTheme.extraColorScheme.onSurfaceVariantAlt4 else colorHighlight,
        inlineContent = inlineContent,
    )
}

fun Long.toPostStatString(numberFormat: NumberFormat): String {
    return if (this > 0) {
        numberFormat.format(this)
    } else {
        ""
    }
}
