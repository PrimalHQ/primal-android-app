package net.primal.android.core.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun PrimalTopAppBar(
    title: String,
    navigationIcon: ImageVector,
    onNavigationIconClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            AppBarIcon(
                icon = navigationIcon,
                onClick = onNavigationIconClick,
            )
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