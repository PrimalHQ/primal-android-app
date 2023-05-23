package net.primal.android.core.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import net.primal.android.R
import net.primal.android.theme.PrimalTheme

@Composable
fun ToolbarIcon(
    iconPainter: Painter,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
) {
    IconButton(enabled = enabled, onClick = onClick) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = tint
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewToolbarIcon() {
    PrimalTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "App name")
                    },
                    actions = {
                        ToolbarIcon(
                            iconPainter = painterResource(id = R.drawable.ic_feed_picker),
                            onClick = {},
                        )
                    },
                )
            }
        ) {
            Surface(modifier = Modifier.padding(it)) { }
        }
    }
}