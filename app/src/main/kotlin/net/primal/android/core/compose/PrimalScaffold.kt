package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import net.primal.android.stream.player.LocalStreamState


@Composable
fun PrimalScaffold(
    modifier: Modifier = Modifier,
    isTopLevelScreen: Boolean = false,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val streamState = LocalStreamState.current
    val localDensity = LocalDensity.current

    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val bottomBarMeasureHeightModifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        bottomBarHeight = with(localDensity) { layoutCoordinates.size.height.toDp() }
    }

    streamState.bottomPadding = bottomBarHeight

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            Box(modifier = bottomBarMeasureHeightModifier) {
                bottomBar()
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(
                        bottom = (streamState.miniPlayerHeight - bottomBarHeight).coerceAtLeast(0.dp),
                    ),
            ) {
                floatingActionButton()
            }
        },
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}
