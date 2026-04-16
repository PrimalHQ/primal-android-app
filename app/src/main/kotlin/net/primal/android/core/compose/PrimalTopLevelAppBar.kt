package net.primal.android.core.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

private const val SWITCH_ANIMATION_DURATION_MS = 300
private const val SWITCH_ANIMATION_MIDPOINT = 0.5f
private val AvatarSwipeThreshold = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimalTopLevelAppBar(
    title: String,
    avatarCdnImage: CdnImage?,
    avatarLegendaryCustomization: LegendaryCustomization?,
    avatarBlossoms: List<String>,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    showTitleChevron: Boolean = false,
    chevronExpanded: Boolean = false,
    onTitleClick: (() -> Unit)? = null,
    onAvatarSwipeDown: (() -> Unit)? = null,
    showDivider: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val effectiveTitle = titleOverride ?: title
    val effectiveSubtitle = subtitleOverride ?: subtitle
    val effectiveShowChevron = if (titleOverride != null) false else showTitleChevron
    val effectiveOnTitleClick = if (titleOverride != null) null else onTitleClick
    val chevronRotation by animateFloatAsState(
        targetValue = if (chevronExpanded) 180f else 0f,
        label = "ChevronRotation",
    )

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                AppBarTitle(
                    title = effectiveTitle,
                    subtitle = effectiveSubtitle,
                    showChevron = effectiveShowChevron,
                    chevronRotation = chevronRotation,
                    onTitleClick = effectiveOnTitleClick,
                )
            },
            actions = {
                SwipeableAvatar(
                    avatarCdnImage = avatarCdnImage,
                    avatarBlossoms = avatarBlossoms,
                    avatarLegendaryCustomization = avatarLegendaryCustomization,
                    onAvatarClick = onAvatarClick,
                    onAvatarSwipeDown = onAvatarSwipeDown,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppTheme.colorScheme.surface,
                scrolledContainerColor = AppTheme.colorScheme.surface,
            ),
            scrollBehavior = scrollBehavior,
        )

        if (showDivider) {
            PrimalDivider()
        }
    }
}

@Composable
private fun AppBarTitle(
    title: String,
    subtitle: String?,
    showChevron: Boolean,
    chevronRotation: Float,
    onTitleClick: (() -> Unit)?,
) {
    val titleColumnModifier = if (onTitleClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onTitleClick,
        )
    } else {
        Modifier
    }

    Column(
        modifier = titleColumnModifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = AppTheme.typography.headlineSmall.copy(
                    fontSize = 25.sp,
                    lineHeight = 25.sp,
                ),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showChevron) {
                Icon(
                    modifier = Modifier.rotate(chevronRotation),
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            }
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    letterSpacing = 0.sp,
                ),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun SwipeableAvatar(
    avatarCdnImage: CdnImage?,
    avatarBlossoms: List<String>,
    avatarLegendaryCustomization: LegendaryCustomization?,
    onAvatarClick: () -> Unit,
    onAvatarSwipeDown: (() -> Unit)?,
) {
    val scope = rememberCoroutineScope()
    val switchProgress = remember { Animatable(0f) }

    val swipeModifier = if (onAvatarSwipeDown != null) {
        Modifier.pointerInput(Unit) {
            val thresholdPx = AvatarSwipeThreshold.toPx()
            var acc = 0f
            var fired = false
            detectDragGestures(
                onDragStart = {
                    acc = 0f
                    fired = false
                },
                onDragCancel = {
                    acc = 0f
                    fired = false
                },
                onDragEnd = {
                    acc = 0f
                    fired = false
                },
            ) { _, drag ->
                acc += drag.y
                if (!fired && acc >= thresholdPx) {
                    fired = true
                    scope.launch {
                        onAvatarSwipeDown.invoke()
                        switchProgress.snapTo(0f)
                        switchProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = SWITCH_ANIMATION_DURATION_MS),
                        )
                        switchProgress.snapTo(0f)
                    }
                }
            }
        }
    } else {
        Modifier
    }

    val progress = switchProgress.value
    val scale = if (progress <= SWITCH_ANIMATION_MIDPOINT) {
        1f - (progress / SWITCH_ANIMATION_MIDPOINT)
    } else {
        (progress - SWITCH_ANIMATION_MIDPOINT) / SWITCH_ANIMATION_MIDPOINT
    }
    val slideDown = if (progress <= SWITCH_ANIMATION_MIDPOINT) {
        progress / SWITCH_ANIMATION_MIDPOINT
    } else {
        0f
    }

    Box(
        modifier = swipeModifier.padding(end = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        UniversalAvatarThumbnail(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = slideDown * size.height * SWITCH_ANIMATION_MIDPOINT
                }
                .clip(CircleShape),
            avatarCdnImage = avatarCdnImage,
            avatarSize = 40.dp,
            avatarBlossoms = avatarBlossoms,
            legendaryCustomization = avatarLegendaryCustomization,
            onClick = onAvatarClick,
        )
    }
}
