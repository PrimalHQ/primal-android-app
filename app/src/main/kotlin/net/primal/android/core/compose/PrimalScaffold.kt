package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.layout.onSizeChanged
import net.primal.android.stream.player.LocalStreamState

@Composable
fun PrimalScaffold(
    modifier: Modifier = Modifier,
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

    var bottomBarHeight by remember { mutableStateOf<Int?>(null) }
    var topBarHeight by remember { mutableStateOf<Int?>(null) }

    bottomBarHeight?.let { streamState.bottomBarHeight = it }
    topBarHeight?.let { streamState.topBarHeight = it }

    Scaffold(
        modifier = modifier,
        topBar = {
            Box(
                modifier = Modifier.onSizeChanged { size ->
                    topBarHeight = size.height
                },
            ) {
                topBar()
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                    bottomBarHeight = layoutCoordinates.size.height
                },
            ) {
                bottomBar()
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}
