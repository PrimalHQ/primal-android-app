package net.primal.android.drawer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import net.primal.android.core.compose.PrimalNavigationBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import kotlin.math.roundToInt

val PrimalBottomBarHeightDp = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimalDrawerScaffold(
    drawerState: DrawerState,
    activeDestination: PrimalTopLevelDestination,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onActiveDestinationClick: () -> Unit = {},
    topBar: @Composable (TopAppBarScrollBehavior?) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
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
            val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }
            val bottomBarNestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        val delta = available.y
                        val newOffset = bottomBarOffsetHeightPx.value + delta
                        bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                        onBottomBarOffsetChange(bottomBarOffsetHeightPx.value)
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
                    PrimalNavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(bottomBarHeight)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -bottomBarOffsetHeightPx.value.roundToInt()
                                )
                            },
                        activeDestination = activeDestination,
                        onTopLevelDestinationChanged = onPrimaryDestinationChanged,
                        onActiveDestinationClick = onActiveDestinationClick,
                    )
                },
                floatingActionButton = floatingActionButton,
            )
        }
    )
}
