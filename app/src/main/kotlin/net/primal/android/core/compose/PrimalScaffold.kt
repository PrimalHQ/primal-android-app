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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import net.primal.android.stream.player.LocalStreamState

@Composable
fun PrimalScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val streamState = LocalStreamState.current

    Scaffold(
        modifier = modifier,
        topBar = {
            topBar?.let {
                Box(
                    modifier = Modifier.onSizeChanged { size ->
                        streamState.topBarHeight = size.height
                    },
                ) {
                    topBar()
                }
            } ?: run {
                streamState.topBarHeight = 0
            }
        },
        bottomBar = {
            bottomBar?.let {
                Box(
                    modifier = Modifier.onSizeChanged { size ->
                        streamState.bottomBarHeight = size.height
                    },
                ) {
                    bottomBar()
                }
            } ?: run {
                streamState.bottomBarHeight = 0
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
