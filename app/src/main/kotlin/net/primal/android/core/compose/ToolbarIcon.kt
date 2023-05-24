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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.theme.PrimalTheme

@Composable
fun ToolbarIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
) {
    IconButton(enabled = enabled, onClick = onClick) {
        Icon(
            imageVector = icon,
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
                            icon = PrimalIcons.Settings,
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