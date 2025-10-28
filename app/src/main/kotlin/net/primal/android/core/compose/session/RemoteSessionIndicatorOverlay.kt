package net.primal.android.core.compose.session

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.animatableSaver
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NostrConnectSession
import net.primal.android.theme.AppTheme

internal const val SHOW_NOTICE_DURATION_TIME_IN_MILLIS = 3000L
internal const val ANIMATION_DURATION = 400
internal const val START_ANIMATION_OFFSET_Y = -100f
internal const val END_ANIMATION_OFFSET_X = 1000f

@Composable
fun RemoteSessionIndicatorOverlay(
    viewModel: RemoteSessionIndicatorViewModel = hiltViewModel(),
    isConnectionUnavailable: Boolean,
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()

    RemoteSessionIndicatorOverlay(
        state = uiState,
        isConnectionUnavailable = isConnectionUnavailable,
        content = content,
    )
}

@Composable
private fun RemoteSessionIndicatorOverlay(
    state: RemoteSessionIndicatorContract.UiState,
    isConnectionUnavailable: Boolean,
    content: @Composable () -> Unit,
) {
    var showFloatingIcon by rememberSaveable { mutableStateOf(false) }
    var hasShownNotice by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isRemoteSessionActive) {
        if (state.isRemoteSessionActive) {
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

        if (state.isRemoteSessionActive && !hasShownNotice) {
            RemoteSessionNotice(modifier = Modifier.align(Alignment.TopCenter))
        }

        if (state.isRemoteSessionActive && showFloatingIcon) {
            FloatingRemoteSessionOverlay(
                modifier = Modifier.align(Alignment.TopEnd),
                isConnectionUnavailable = isConnectionUnavailable,
            )
        }
    }
}

@Composable
private fun RemoteSessionNotice(modifier: Modifier = Modifier) {
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
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = PrimalIcons.NostrConnectSession,
            contentDescription = null,
            tint = AppTheme.colorScheme.surfaceVariant,
        )
        Text(
            text = stringResource(id = R.string.app_nostr_connect_session_active),
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun FloatingRemoteSessionOverlay(modifier: Modifier = Modifier, isConnectionUnavailable: Boolean) {
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
        targetValue = if (isConnectionUnavailable) 64.dp else 16.dp,
        label = "AnimatedTopPadding",
    )

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val overlayWidth = overlaySize.width.toFloat()
                        val overlayHeight = overlaySize.height.toFloat()
                        val maxOffsetX = (screenWidthPx - overlayWidth).coerceAtLeast(0f)

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
            .onGloballyPositioned { coordinates ->
                overlaySize = coordinates.size
            }
            .padding(top = WindowInsets.safeContent.asPaddingValues().calculateTopPadding())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(top = animatedTopPadding, end = 16.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1)
            .padding(10.dp),
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            imageVector = PrimalIcons.NostrConnectSession,
            contentDescription = null,
            tint = AppTheme.colorScheme.onPrimary,
        )
    }
}
