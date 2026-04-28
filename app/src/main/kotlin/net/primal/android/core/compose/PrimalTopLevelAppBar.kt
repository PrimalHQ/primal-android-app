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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

private val WIPE_BAR_WIDTH = 20.dp
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
    pagerState: PagerState? = null,
    pages: List<AppBarPage> = emptyList(),
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
                if (titleOverride != null) {
                    AppBarTitle(
                        title = effectiveTitle,
                        subtitle = effectiveSubtitle,
                        showChevron = effectiveShowChevron,
                        chevronRotation = chevronRotation,
                        onTitleClick = effectiveOnTitleClick,
                    )
                } else if (pagerState != null && pages.size > 1) {
                    SwipingAppBarTitle(
                        pagerState = pagerState,
                        pages = pages,
                        showChevron = effectiveShowChevron,
                        chevronRotation = chevronRotation,
                        onTitleClick = effectiveOnTitleClick,
                    )
                } else {
                    AppBarTitle(
                        title = effectiveTitle,
                        subtitle = effectiveSubtitle,
                        showChevron = effectiveShowChevron,
                        chevronRotation = chevronRotation,
                        onTitleClick = effectiveOnTitleClick,
                    )
                }
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
    modifier: Modifier = Modifier,
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
        modifier = titleColumnModifier.then(modifier),
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

data class AppBarPage(val title: String, val subtitle: String? = null)

@Composable
private fun SwipingAppBarTitle(
    pagerState: PagerState,
    pages: List<AppBarPage>,
    showChevron: Boolean,
    chevronRotation: Float,
    onTitleClick: (() -> Unit)?,
) {
    val maxIndex = (pages.size - 1).coerceAtLeast(0)
    val settledPage = pagerState.settledPage.coerceIn(0, maxIndex)

    val current = pages.getOrNull(settledPage)
    val prev = if (settledPage > 0) pages.getOrNull(settledPage - 1) else null
    val next = if (settledPage < maxIndex) pages.getOrNull(settledPage + 1) else null

    Box(modifier = Modifier.clipToBounds()) {
        AppBarTitle(
            modifier = Modifier
                .fillMaxWidth()
                .wipeClip(pagerState = pagerState, settledPage = settledPage, role = WipeRole.CURRENT),
            title = current?.title.orEmpty(),
            subtitle = current?.subtitle?.ifBlank { null },
            showChevron = showChevron,
            chevronRotation = chevronRotation,
            onTitleClick = onTitleClick,
        )

        if (prev != null) {
            AppBarTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .wipeClip(pagerState = pagerState, settledPage = settledPage, role = WipeRole.PREV),
                title = prev.title,
                subtitle = prev.subtitle?.ifBlank { null },
                showChevron = showChevron,
                chevronRotation = chevronRotation,
                onTitleClick = onTitleClick,
            )
        }

        if (next != null) {
            AppBarTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .wipeClip(pagerState = pagerState, settledPage = settledPage, role = WipeRole.NEXT),
                title = next.title,
                subtitle = next.subtitle?.ifBlank { null },
                showChevron = showChevron,
                chevronRotation = chevronRotation,
                onTitleClick = onTitleClick,
            )
        }
    }
}

private enum class WipeRole { CURRENT, PREV, NEXT }

private fun Modifier.wipeClip(
    pagerState: PagerState,
    settledPage: Int,
    role: WipeRole,
): Modifier =
    drawWithContent {
        val offset = (pagerState.currentPage - settledPage).toFloat() + pagerState.currentPageOffsetFraction
        if (role == WipeRole.NEXT && offset <= 0f) return@drawWithContent
        if (role == WipeRole.PREV && offset >= 0f) return@drawWithContent

        val width = size.width
        val halfBar = WIPE_BAR_WIDTH.toPx() / 2f
        val progress = abs(offset).coerceIn(0f, 1f)
        val swipingForward = offset > 0f
        val wipeCenter = if (swipingForward) {
            width + halfBar - progress * (width + halfBar * 2f)
        } else {
            -halfBar + progress * (width + halfBar * 2f)
        }
        val leadingEdge = (wipeCenter - halfBar).coerceAtLeast(0f)
        val trailingEdge = (wipeCenter + halfBar).coerceAtMost(width)

        when (role) {
            WipeRole.CURRENT -> if (swipingForward) {
                clipRect(right = leadingEdge) { this@drawWithContent.drawContent() }
            } else {
                clipRect(left = trailingEdge) { this@drawWithContent.drawContent() }
            }
            WipeRole.NEXT -> clipRect(left = trailingEdge) { this@drawWithContent.drawContent() }
            WipeRole.PREV -> clipRect(right = leadingEdge) { this@drawWithContent.drawContent() }
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
