package net.primal.android.core.compose.indicator

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.core.compose.animatableSaver
import net.primal.android.theme.AppTheme

internal const val SHOW_NOTICE_DURATION_TIME_IN_MILLIS = 3000L
internal const val ANIMATION_DURATION = 400
internal const val START_ANIMATION_OFFSET_Y = -100f
internal const val END_ANIMATION_OFFSET_X = 1000f

@Composable
fun IndicatorOverlay(
    showIndicator: Boolean,
    indicatorText: String,
    indicatorIcon: ImageVector,
    indicatorIconTint: Color = AppTheme.colorScheme.surfaceVariant,
    floatingIcon: ImageVector,
    floatingIconTint: Color = AppTheme.colorScheme.onPrimary,
    floatingIconTopPadding: Dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var showFloatingIcon by rememberSaveable(showIndicator) { mutableStateOf(false) }
    var hasShownNotice by rememberSaveable(showIndicator) { mutableStateOf(false) }

    LaunchedEffect(showIndicator) {
        if (showIndicator) {
            showFloatingIcon = false
            hasShownNotice = false
            delay(SHOW_NOTICE_DURATION_TIME_IN_MILLIS)
            showFloatingIcon = true
            delay(ANIMATION_DURATION.toLong())
            hasShownNotice = true
        } else {
            showFloatingIcon = false
            hasShownNotice = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (showIndicator && !hasShownNotice) {
            IndicatorNotice(
                modifier = Modifier.align(Alignment.TopCenter),
                text = indicatorText,
                icon = indicatorIcon,
                iconTint = indicatorIconTint,
            )
        }

        if (showIndicator && showFloatingIcon) {
            FloatingIndicatorIcon(
                modifier = Modifier.align(Alignment.TopEnd),
                icon = floatingIcon,
                iconTint = floatingIconTint,
                topPadding = floatingIconTopPadding,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun IndicatorNotice(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    iconTint: Color,
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(START_ANIMATION_OFFSET_Y) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        offsetX.snapTo(0f)
        offsetY.snapTo(START_ANIMATION_OFFSET_Y)
        alpha.snapTo(0f)

        launch { offsetY.animateTo(0f, animationSpec = tween(durationMillis = ANIMATION_DURATION)) }
        launch { alpha.animateTo(1f, animationSpec = tween(durationMillis = ANIMATION_DURATION)) }

        delay(SHOW_NOTICE_DURATION_TIME_IN_MILLIS)

        launch {
            offsetX.animateTo(
                END_ANIMATION_OFFSET_X,
                animationSpec = tween(durationMillis = ANIMATION_DURATION),
            )
        }
        launch { alpha.animateTo(0f, animationSpec = tween(durationMillis = ANIMATION_DURATION)) }
    }

    Row(
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                this.alpha = alpha.value
            }
            .padding(top = WindowInsets.safeContent.asPaddingValues().calculateTopPadding())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(top = 16.dp, end = 16.dp)
            .clip(CircleShape)
            .background(AppTheme.colorScheme.onPrimary)
            .height(40.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
        )
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun FloatingIndicatorIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    topPadding: Dp,
    onClick: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val displayMetrics = LocalResources.current.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )

    val offsetX = rememberSaveable(saver = animatableSaver()) { Animatable(0f) }
    val offsetY = rememberSaveable(saver = animatableSaver()) { Animatable(0f) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    val animatedTopPadding by animateDpAsState(
        targetValue = topPadding,
        label = "AnimatedTopPadding",
    )

    val horizontalPaddingPx = with(LocalDensity.current) { 16.dp.toPx() }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
            .padding(top = WindowInsets.safeContent.asPaddingValues().calculateTopPadding())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(top = animatedTopPadding, end = 16.dp)
            .size(40.dp)
            .onGloballyPositioned { coordinates ->
                overlaySize = coordinates.size
            }
            .systemGestureExclusion()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val overlayWidth = overlaySize.width.toFloat()
                        val overlayHeight = overlaySize.height.toFloat()
                        val maxOffsetX = (screenWidthPx - overlayWidth - (2 * horizontalPaddingPx))
                            .coerceAtLeast(0f)

                        val currentRightEdge = screenWidthPx + offsetX.value
                        val snapToRight = currentRightEdge > screenWidthPx / 2

                        val targetX = if (snapToRight) 0f else -maxOffsetX
                        val targetY = offsetY.value.coerceIn(0f, (screenHeightPx - overlayHeight))

                        scope.launch { offsetX.animateTo(targetX, animationSpec = springSpec) }
                        scope.launch { offsetY.animateTo(targetY, animationSpec = springSpec) }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                )
            }
            .clip(CircleShape)
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() },
            )
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1)
            .padding(10.dp),
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
        )
    }
}
