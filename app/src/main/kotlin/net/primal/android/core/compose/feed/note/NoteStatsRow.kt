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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
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
    postStats: FeedPostStatsUi,
    onPostAction: (FeedPostAction) -> Unit,
    onPostLongPressAction: (FeedPostAction) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SinglePostStat(
            textCount = postStats.repliesCount.toPostStatString(),
            highlighted = postStats.userReplied,
            iconVector = PrimalIcons.FeedReplies,
            iconVectorHighlight = PrimalIcons.FeedRepliesFilled,
            colorHighlight = AppTheme.extraColorScheme.replied,
            onClick = { onPostAction(FeedPostAction.Reply) },
            onLongClick = { onPostLongPressAction(FeedPostAction.Reply) },
        )

        SinglePostStat(
            textCount = postStats.satsZapped.toPostStatString(),
            highlighted = postStats.userZapped,
            iconVector = PrimalIcons.FeedZaps,
            iconVectorHighlight = PrimalIcons.FeedZapsFilled,
            colorHighlight = AppTheme.extraColorScheme.zapped,
            onClick = { onPostAction(FeedPostAction.Zap) },
            onLongClick = { onPostLongPressAction(FeedPostAction.Zap) },
        )

        SinglePostStat(
            textCount = postStats.likesCount.toPostStatString(),
            highlighted = postStats.userLiked,
            iconVector = PrimalIcons.FeedLikes,
            iconVectorHighlight = PrimalIcons.FeedLikesFilled,
            colorHighlight = AppTheme.extraColorScheme.liked,
            onClick = {
                if (!postStats.userLiked) {
                    onPostAction(FeedPostAction.Like)
                }
            },
            onLongClick = { onPostLongPressAction(FeedPostAction.Like) },
        )

        SinglePostStat(
            textCount = postStats.repostsCount.toPostStatString(),
            highlighted = postStats.userReposted,
            iconVector = PrimalIcons.FeedReposts,
            iconVectorHighlight = PrimalIcons.FeedRepostsFilled,
            colorHighlight = AppTheme.extraColorScheme.reposted,
            onClick = { onPostAction(FeedPostAction.Repost) },
            onLongClick = { onPostLongPressAction(FeedPostAction.Repost) },
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
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
                    contentDescription = null,
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
                onClick = onClick,
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

private fun Long.toPostStatString(): String = if (this > 0) toString() else ""
