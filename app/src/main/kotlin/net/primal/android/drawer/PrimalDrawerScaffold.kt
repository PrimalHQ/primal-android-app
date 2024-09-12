package net.primal.android.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.PrimalNavigationBarLightningBolt
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.user.domain.Badges

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimalDrawerScaffold(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    activeDestination: PrimalTopLevelDestination,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
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
    focusModeEnabled: Boolean = true,
) {
    val localDensity = LocalDensity.current

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

    var bottomBarInitialHeight by remember { mutableStateOf(0.dp) }
    val bottomBarMeasureHeightModifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        bottomBarInitialHeight = with(localDensity) { layoutCoordinates.size.height.toDp() }
    }
    val bottomBarRealHeight by remember(topAppBarState) {
        derivedStateOf {
            with(localDensity) {
                ((1 - topAppBarState.collapsedFraction) * bottomBarInitialHeight.roundToPx()).toDp()
            }
        }
    }

    val focusModeOn by remember(topAppBarState) {
        derivedStateOf { topAppBarState.collapsedFraction > 0.5f }
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            PrimalDrawer(
                drawerState = drawerState,
                onDrawerDestinationClick = onDrawerDestinationClick,
                onQrCodeClick = onDrawerQrCodeClick,
            )
        },
        content = {
            Scaffold(
                modifier = if (focusModeEnabled) {
                    Modifier
                        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                        .navigationBarsPadding()
                } else {
                    Modifier.navigationBarsPadding()
                },
                topBar = { topAppBar(if (focusModeEnabled) topAppBarScrollBehavior else null) },
                content = { paddingValues ->
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
                                    this.alpha = (1 - topAppBarState.collapsedFraction) * 1.0f
                                },
                        ) {
                            floatingNewDataHost()
                        }
                    }
                },
                bottomBar = {
                    PrimalNavigationBarLightningBolt(
                        modifier = Modifier
                            .then(if (bottomBarInitialHeight == 0.dp) bottomBarMeasureHeightModifier else Modifier)
                            .then(
                                if (bottomBarInitialHeight > 0.dp) {
                                    Modifier.offset(
                                        y = bottomBarInitialHeight - bottomBarRealHeight,
                                    )
                                } else {
                                    Modifier
                                },
                            ),
                        activeDestination = activeDestination,
                        onTopLevelDestinationChanged = onPrimaryDestinationChanged,
                        onActiveDestinationClick = onActiveDestinationClick,
                        badges = badges,
                    )
                },
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = !focusModeOn,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .graphicsLayer {
                                this.alpha = (1 - topAppBarState.collapsedFraction) * 1.0f
                                this.translationY = (bottomBarInitialHeight - bottomBarRealHeight).toPx()
                                this.rotationZ = (bottomBarInitialHeight - bottomBarRealHeight).toPx().coerceIn(
                                    minimumValue = 0f,
                                    maximumValue = 45f,
                                )
                                this.clip = false
                            },
                    ) {
                        floatingActionButton()
                    }
                },
                snackbarHost = snackbarHost,
            )
        },
    )
}

val FloatingNewDataHostTopPadding = 42.dp
