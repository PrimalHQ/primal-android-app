package net.primal.android.notes.feed.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.FadingEdge
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.fadingEdge
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Follow
import net.primal.android.notes.feed.model.StreamPillUi
import net.primal.android.theme.AppTheme

@Composable
fun StreamPillsRow(
    modifier: Modifier = Modifier,
    streamPills: List<StreamPillUi>,
    onClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val padding = 10.dp
    val itemSpacedBy = 10.dp
    val avatarSize = 42.dp
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val localDensity = LocalDensity.current
    val screenWidthPx = displayMetrics.widthPixels
    val paddingPx = with(localDensity) { padding.toPx() }
    val itemSpacedByPx = with(localDensity) { itemSpacedBy.toPx() }
    val avatarSizePx = with(localDensity) { avatarSize.toPx() }

    val itemWidth = remember(streamPills) {
        with(localDensity) {
            if (streamPills.size == 1) {
                screenWidthPx - 2 * paddingPx
            } else {
                screenWidthPx - 2 * paddingPx - itemSpacedByPx - avatarSizePx
            }.toDp()
        }
    }

    AnimatedVisibility(
        visible = streamPills.isNotEmpty(),
        enter = slideInVertically(),
        exit = slideOutVertically(targetOffsetY = { -it }),
    ) {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(itemSpacedBy),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(all = padding),
        ) {
            items(
                count = streamPills.size,
                key = { streamPills[it].naddr },
            ) { index ->
                val item = streamPills[index]

                PulsingStreamPill(
                    modifier = Modifier.animateItem(),
                    avatarSize = avatarSize,
                    itemWidth = itemWidth,
                    streamPill = item,
                    onClick = onClick,
                    onProfileClick = onProfileClick,
                )
            }
        }
        PrimalDivider()
    }
}

private val STREAM_PILL_WIDTH_DELTA = 4.dp
private val MAX_STREAM_PILL_HEIGHT = 52.dp

@Composable
private fun PulsingStreamPill(
    modifier: Modifier = Modifier,
    itemWidth: Dp,
    avatarSize: Dp,
    streamPill: StreamPillUi,
    onClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val animatedWidth by rememberPulsingDp(from = itemWidth, to = itemWidth + STREAM_PILL_WIDTH_DELTA)
    val animatedHeight by rememberPulsingDp(from = MAX_STREAM_PILL_HEIGHT - 4.dp, to = MAX_STREAM_PILL_HEIGHT)

    Box(
        modifier = Modifier.size(width = itemWidth + STREAM_PILL_WIDTH_DELTA, height = MAX_STREAM_PILL_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = modifier
                .animateContentSize()
                .width(animatedWidth)
                .height(animatedHeight)
                .clip(CircleShape)
                .background(AppTheme.colorScheme.primary),
        )

        StreamPill(
            modifier = modifier,
            avatarSize = avatarSize,
            itemWidth = itemWidth,
            streamPill = streamPill,
            onClick = onClick,
            onProfileClick = onProfileClick,
        )
    }
}

@Composable
private fun StreamPill(
    modifier: Modifier = Modifier,
    avatarSize: Dp,
    itemWidth: Dp,
    streamPill: StreamPillUi,
    onClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.primal_stream_audio_widget),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
        speed = 1.0f,
    )

    Row(
        modifier = modifier
            .animateContentSize()
            .width(itemWidth)
            .clip(CircleShape)
            .background(AppTheme.colorScheme.primary)
            .padding(2.dp)
            .padding(end = 16.dp)
            .clickable { onClick(streamPill.naddr) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UniversalAvatarThumbnail(
            avatarSize = avatarSize,
            fallbackBorderColor = Color.White,
            borderSizeOverride = 2.dp,
            avatarCdnImage = streamPill.hostAvatarCdnImage,
            onClick = { onProfileClick(streamPill.hostProfileId) },
        )

        IconText(
            text = numberFormat.format(streamPill.currentParticipants),
            leadingIcon = PrimalIcons.Follow,
            color = Color.White,
            iconSize = 18.sp,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            modifier = Modifier
                .padding(start = 8.dp)
                .fadingEdge(
                    edge = FadingEdge.End,
                    color = AppTheme.colorScheme.primary,
                    length = 24.dp,
                )
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    velocity = 64.dp,
                )
                .weight(1f),
            maxLines = 1,
            text = streamPill.title,
            style = AppTheme.typography.bodyLarge,
            color = Color.White,
        )

        LottieAnimation(
            modifier = Modifier.size(24.dp),
            composition = composition,
            progress = { progress },
        )
    }
}

@Composable
private fun rememberPulsingDp(
    from: Dp,
    to: Dp,
    durationMillis: Int = 600,
    delayMillis: Int = 600,
    easing: Easing = LinearEasing,
): State<Dp> {
    val t = rememberInfiniteTransition(label = "pulse")
    return t.animateValue(
        initialValue = from,
        targetValue = to,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, delayMillis, easing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dp",
    )
}
