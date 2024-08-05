package net.primal.android.core.compose.feed.note

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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.feed.model.EventStatsUi
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedLikes
import net.primal.android.core.compose.icons.primaliconpack.FeedLikesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedReplies
import net.primal.android.core.compose.icons.primaliconpack.FeedRepliesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.core.compose.icons.primaliconpack.FeedRepostsFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.FeedZapsFilled
import net.primal.android.theme.AppTheme

@Composable
fun FeedNoteStatsRow(
    modifier: Modifier,
    eventStats: EventStatsUi,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongPressAction: ((FeedPostAction) -> Unit)? = null,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SinglePostStat(
            textCount = eventStats.repliesCount.toPostStatString(numberFormat),
            highlighted = eventStats.userReplied,
            iconVector = PrimalIcons.FeedReplies,
            iconVectorHighlight = PrimalIcons.FeedRepliesFilled,
            colorHighlight = AppTheme.extraColorScheme.replied,
            onClick = onPostAction?.let {
                { onPostAction(FeedPostAction.Reply) }
            },
            onLongClick = onPostLongPressAction?.let {
                { onPostLongPressAction(FeedPostAction.Reply) }
            },
            iconContentDescription = stringResource(id = R.string.accessibility_replies_count),
        )

        SinglePostStat(
            textCount = eventStats.satsZapped.toPostStatString(numberFormat),
            highlighted = eventStats.userZapped,
            iconVector = PrimalIcons.FeedZaps,
            iconVectorHighlight = PrimalIcons.FeedZapsFilled,
            colorHighlight = AppTheme.extraColorScheme.zapped,
            onClick = onPostAction?.let {
                { onPostAction(FeedPostAction.Zap) }
            },
            onLongClick = onPostLongPressAction?.let {
                { onPostLongPressAction(FeedPostAction.Zap) }
            },
            iconContentDescription = stringResource(id = R.string.accessibility_zaps_count),
        )

        SinglePostStat(
            textCount = eventStats.likesCount.toPostStatString(numberFormat),
            highlighted = eventStats.userLiked,
            iconVector = PrimalIcons.FeedLikes,
            iconVectorHighlight = PrimalIcons.FeedLikesFilled,
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

        SinglePostStat(
            textCount = eventStats.repostsCount.toPostStatString(numberFormat),
            highlighted = eventStats.userReposted,
            iconVector = PrimalIcons.FeedReposts,
            iconVectorHighlight = PrimalIcons.FeedRepostsFilled,
            colorHighlight = AppTheme.extraColorScheme.reposted,
            onClick = onPostAction?.let {
                { onPostAction(FeedPostAction.Repost) }
            },
            onLongClick = onPostLongPressAction?.let {
                { onPostLongPressAction(FeedPostAction.Repost) }
            },
            iconContentDescription = stringResource(id = R.string.accessibility_repost_count),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SinglePostStat(
    textCount: String,
    highlighted: Boolean,
    iconVector: ImageVector,
    iconVectorHighlight: ImageVector,
    colorHighlight: Color,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    iconContentDescription: String? = null,
) {
    val titleText = buildAnnotatedString {
        appendInlineContent("icon", "[icon]")
        append(' ')
        append(textCount)
    }

    val inlineContent = mapOf(
        "icon" to InlineTextContent(
            placeholder = Placeholder(
                24.sp, 24.sp, PlaceholderVerticalAlign.TextCenter,
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
        style = AppTheme.typography.bodyMedium,
        color = if (!highlighted) AppTheme.extraColorScheme.onSurfaceVariantAlt4 else colorHighlight,
        inlineContent = inlineContent,
    )
}

private fun Long.toPostStatString(numberFormat: NumberFormat): String {
    return if (this > 0) {
        numberFormat.format(this)
    } else {
        ""
    }
}
