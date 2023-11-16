package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun AppBarIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    enabledBackgroundColor: Color = Color.Unspecified,
    disabledBackgroundColor: Color = AppTheme.colorScheme.outline,
) {
    IconButton(
        modifier = modifier
            .clip(CircleShape)
            .background(
                color = if (enabled) enabledBackgroundColor else disabledBackgroundColor,
            ),
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
            tint = tint,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewAppBarIcon() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "App name")
                    },
                    actions = {
                        AppBarIcon(
                            icon = PrimalIcons.Settings,
                            onClick = {},
                        )
                    },
                )
            },
        ) {
            Surface(modifier = Modifier.padding(it)) { }
        }
    }
}
