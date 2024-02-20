package net.primal.android.wallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.PrimalCircleButton
import net.primal.android.theme.AppTheme

val WalletTabsHeight = 72.dp

@Composable
fun WalletTabsBar(
    modifier: Modifier = Modifier,
    tabs: List<WalletTab>,
    activeTab: WalletTab,
    onTabClick: (WalletTab) -> Unit,
    tabIconSize: Dp = 24.dp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach {
            val isSelected = activeTab == it
            WalletTabButton(
                icon = if (isSelected) it.selectedIcon else it.unselectedIcon,
                iconSize = tabIconSize,
                selected = isSelected,
                onClick = { onTabClick(it) },
            )
        }
    }
}

@Composable
fun WalletTabButton(
    icon: ImageVector,
    iconSize: Dp,
    selected: Boolean,
    onClick: () -> Unit,
) {
    PrimalCircleButton(
        modifier = Modifier.size(56.dp),
        containerColor = if (selected) AppTheme.colorScheme.onSurface else AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = if (selected) AppTheme.colorScheme.surface else AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = icon,
            contentDescription = null,
        )
    }
}
