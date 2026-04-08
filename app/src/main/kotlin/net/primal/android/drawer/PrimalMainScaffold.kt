package net.primal.android.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.NavigationBarFullHeightDp
import net.primal.android.core.compose.PrimalNavigationBar
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.user.domain.Badges

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimalMainScaffold(
    modifier: Modifier = Modifier,
    activeDestination: PrimalTopLevelDestination,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    badges: Badges = Badges(),
    onActiveDestinationClick: () -> Unit = {},
    topAppBarState: TopAppBarState = remember {
        TopAppBarState(
            initialHeightOffsetLimit = -Float.MAX_VALUE,
            initialHeightOffset = 0f,
            initialContentOffset = 0f,
        )
    },
    topAppBar: @Composable (TopAppBarScrollBehavior?) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {},
    floatingNewDataHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    overlay: @Composable () -> Unit = {},
    focusModeEnabled: Boolean = true,
) {
    val localDensity = LocalDensity.current
    val streamState = LocalStreamState.current
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val bottomBarState = rememberBottomBarState(topAppBarState, localDensity, streamState)
    val focusModeOn by remember(topAppBarState) {
        derivedStateOf { topAppBarState.collapsedFraction > FOCUS_MODE_COLLAPSE_THRESHOLD }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PrimalScaffold(
            modifier = modifier.then(
                if (focusModeEnabled) {
                    Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                } else {
                    Modifier
                },
            ),
            topBar = {
                topAppBar(if (focusModeEnabled) topAppBarScrollBehavior else null)
            },
            propagateBottomBarSize = false,
            content = { paddingValues ->
                ScaffoldContent(
                    paddingValues = paddingValues,
                    focusModeOn = focusModeOn,
                    collapsedFraction = topAppBarState.collapsedFraction,
                    content = content,
                    floatingNewDataHost = floatingNewDataHost,
                )
            },
            bottomBar = {
                ScaffoldBottomBar(
                    isBottomBarVisible = bottomBarState.isVisible,
                    bottomBarInitialHeight = bottomBarState.initialHeight,
                    bottomBarRealHeight = bottomBarState.realHeight,
                    bottomBarMeasureHeightModifier = bottomBarState.measureModifier,
                    activeDestination = activeDestination,
                    onPrimaryDestinationChanged = onPrimaryDestinationChanged,
                    onActiveDestinationClick = onActiveDestinationClick,
                    badges = badges,
                )
            },
            floatingActionButton = {
                ScaffoldFab(
                    focusModeOn = focusModeOn,
                    collapsedFraction = topAppBarState.collapsedFraction,
                    bottomBarInitialHeight = bottomBarState.initialHeight,
                    bottomBarRealHeight = bottomBarState.realHeight,
                    floatingActionButton = floatingActionButton,
                )
            },
            snackbarHost = snackbarHost,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = with(localDensity) { streamState.topBarHeight.toDp() })
                .clipToBounds(),
        ) {
            overlay()
        }
    }
}

@Composable
private fun ScaffoldContent(
    paddingValues: PaddingValues,
    focusModeOn: Boolean,
    collapsedFraction: Float,
    content: @Composable (PaddingValues) -> Unit,
    floatingNewDataHost: @Composable () -> Unit,
) {
    Box {
        content(paddingValues)

        AnimatedVisibility(
            visible = !focusModeOn,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = FloatingNewDataHostTopPadding)
                .wrapContentHeight()
                .wrapContentWidth()
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    this.alpha = (1 - collapsedFraction) * 1.0f
                },
        ) {
            floatingNewDataHost()
        }
    }
}

@Composable
private fun ScaffoldBottomBar(
    isBottomBarVisible: Boolean,
    bottomBarInitialHeight: Dp,
    bottomBarRealHeight: Dp,
    bottomBarMeasureHeightModifier: Modifier,
    activeDestination: PrimalTopLevelDestination,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onActiveDestinationClick: () -> Unit,
    badges: Badges,
) {
    AnimatedVisibility(
        visible = isBottomBarVisible,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        PrimalNavigationBar(
            modifier = if (bottomBarInitialHeight.isZeroOrNavigationBarFullHeight()) {
                bottomBarMeasureHeightModifier
            } else {
                Modifier
            }
                .offset {
                    IntOffset(
                        x = 0.dp.roundToPx(),
                        y = if (bottomBarInitialHeight > 0.dp) {
                            bottomBarInitialHeight - bottomBarRealHeight
                        } else {
                            0.dp
                        }.roundToPx(),
                    )
                },
            activeDestination = activeDestination,
            onTopLevelDestinationChanged = onPrimaryDestinationChanged,
            onActiveDestinationClick = onActiveDestinationClick,
            badges = badges,
        )
    }
}

@Composable
private fun ScaffoldFab(
    focusModeOn: Boolean,
    collapsedFraction: Float,
    bottomBarInitialHeight: Dp,
    bottomBarRealHeight: Dp,
    floatingActionButton: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = !focusModeOn,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .graphicsLayer {
                this.alpha = (1 - collapsedFraction) * 1.0f
                this.translationY = (bottomBarInitialHeight - bottomBarRealHeight).toPx()
                this.rotationZ = (bottomBarInitialHeight - bottomBarRealHeight).toPx().coerceIn(
                    minimumValue = 0f,
                    maximumValue = FAB_MAX_ROTATION_Z,
                )
                this.clip = false
            },
    ) {
        floatingActionButton()
    }
}

private class BottomBarState(
    val initialHeight: Dp,
    val realHeight: Dp,
    val isVisible: Boolean,
    val measureModifier: Modifier,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberBottomBarState(
    topAppBarState: TopAppBarState,
    localDensity: androidx.compose.ui.unit.Density,
    streamState: net.primal.android.stream.player.StreamState,
): BottomBarState {
    var initialHeight by remember { mutableStateOf(0.dp) }
    val measureModifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        initialHeight = with(localDensity) { layoutCoordinates.size.height.toDp() }
    }
    val realHeight by remember(topAppBarState) {
        derivedStateOf {
            with(localDensity) {
                ((1 - topAppBarState.collapsedFraction) * initialHeight.roundToPx()).toDp()
            }
        }
    }

    LaunchedEffect(realHeight) {
        if (initialHeight != 0.dp) {
            streamState.bottomBarHeight = with(localDensity) { realHeight.toPx().toInt() }
        }
    }

    val isVisible by remember {
        derivedStateOf {
            initialHeight.isZeroOrNavigationBarFullHeight() || realHeight > 0.dp
        }
    }

    return BottomBarState(
        initialHeight = initialHeight,
        realHeight = realHeight,
        isVisible = isVisible,
        measureModifier = measureModifier,
    )
}

private const val FOCUS_MODE_COLLAPSE_THRESHOLD = 0.5f
private const val FAB_MAX_ROTATION_Z = 45f

val FloatingNewDataHostTopPadding = 42.dp

private fun Dp.isZeroOrNavigationBarFullHeight() =
    this == 0.dp || (this >= (NavigationBarFullHeightDp - 1.dp) && this <= (NavigationBarFullHeightDp + 1.dp))
