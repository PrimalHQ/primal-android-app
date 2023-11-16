package net.primal.android.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalNavigationBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.user.domain.Badges

val PrimalBottomBarHeightDp = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimalDrawerScaffold(
    drawerState: DrawerState,
    activeDestination: PrimalTopLevelDestination,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    badges: Badges = Badges(),
    showBottomBarDivider: Boolean = true,
    onActiveDestinationClick: () -> Unit = {},
    topBar: @Composable (TopAppBarScrollBehavior?) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    bottomBarHeight: Dp = PrimalBottomBarHeightDp,
    onBottomBarOffsetChange: (Float) -> Unit = {},
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PrimalDrawer(
                drawerState = drawerState,
                onDrawerDestinationClick = onDrawerDestinationClick,
            )
        },
        content = {
            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

            val bottomBarHeightPx = with(LocalDensity.current) {
                bottomBarHeight.roundToPx().toFloat()
            }
            val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
            val bottomBarNestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        val delta = available.y
                        val newOffset = bottomBarOffsetHeightPx.floatValue + delta
                        bottomBarOffsetHeightPx.floatValue = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                        onBottomBarOffsetChange(bottomBarOffsetHeightPx.floatValue)
                        return Offset.Zero
                    }
                }
            }

            Scaffold(
                modifier = Modifier
                    .nestedScroll(bottomBarNestedScrollConnection)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = { topBar(scrollBehavior) },
                content = { paddingValues -> content(paddingValues) },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(bottomBarHeight)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -bottomBarOffsetHeightPx.floatValue.roundToInt(),
                                )
                            },
                    ) {
                        if (showBottomBarDivider) {
                            PrimalDivider()
                        }

                        PrimalNavigationBar(
                            activeDestination = activeDestination,
                            onTopLevelDestinationChanged = onPrimaryDestinationChanged,
                            onActiveDestinationClick = onActiveDestinationClick,
                            badges = badges,
                        )
                    }
                },
                floatingActionButton = floatingActionButton,
                snackbarHost = snackbarHost,
            )
        },
    )
}
