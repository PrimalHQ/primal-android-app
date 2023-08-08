package net.primal.android.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun PrimalTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit,
    navigationIcon: ImageVector? = null,
    avatarUrl: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            if (avatarUrl != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigationIconClick)
                ) {
                    AvatarThumbnailListItemImage(
                        source = avatarUrl,
                        modifier = Modifier.size(32.dp),
                    )
                }
            } else if (navigationIcon != null) {
                AppBarIcon(
                    icon = navigationIcon,
                    onClick = onNavigationIconClick,
                )
            }
        },
        title = {
            Text(text = title)
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppTheme.colorScheme.surface,
            scrolledContainerColor = AppTheme.colorScheme.surface,
        ),
        scrollBehavior = scrollBehavior,
    )
}
