package net.primal.android.profile.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun ProfileAppBarIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    enabledBackgroundColor: Color = Color.Unspecified,
    disabledBackgroundColor: Color = AppTheme.colorScheme.outline,
    appBarIconContentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(
                color = if (enabled) enabledBackgroundColor else disabledBackgroundColor,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = icon,
            contentDescription = appBarIconContentDescription,
            tint = tint,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewProfileAppBarIcon() {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        navigationIcon = {
                            ProfileAppBarIcon(
                                icon = PrimalIcons.ArrowBack,
                                enabledBackgroundColor = Color.Black.copy(alpha = 0.5f),
                                onClick = {},
                            )
                        },
                        title = {
                            Text(text = "App name")
                        },
                        actions = {
                            ProfileAppBarIcon(
                                icon = PrimalIcons.Settings,
                                enabledBackgroundColor = Color.Black.copy(alpha = 0.5f),
                                onClick = {},
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Gray,
                        ),
                    )

                    TopAppBar(
                        navigationIcon = {
                            AppBarIcon(
                                icon = PrimalIcons.ArrowBack,
                                onClick = {},
                            )
                        },
                        title = {
                            Text(text = "App name")
                        },
                        actions = {
                            AppBarIcon(
                                icon = PrimalIcons.Settings,
                                onClick = {},
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Gray,
                        ),
                    )
                }
            },
        ) {
            Surface(modifier = Modifier.padding(it)) { }
        }
    }
}
