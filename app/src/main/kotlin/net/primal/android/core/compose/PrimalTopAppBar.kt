package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.foundation.ClickDebounce
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun PrimalTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    avatarUrl: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showDivider: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Column {
        CenterAlignedTopAppBar(
            modifier = modifier,
            navigationIcon = {
                if (avatarUrl != null) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape)
                    ) {
                        AvatarThumbnailListItemImage(
                            source = avatarUrl,
                            modifier = Modifier.size(32.dp),
                            onClick = onNavigationIconClick,
                        )
                    }
                } else if (navigationIcon != null) {
                    val clickDebounce by remember { mutableStateOf(ClickDebounce()) }
                    AppBarIcon(
                        icon = navigationIcon,
                        onClick = { clickDebounce.processEvent(onNavigationIconClick) },
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

        if (showDivider) {
            PrimalDivider()
        }
    }
}
