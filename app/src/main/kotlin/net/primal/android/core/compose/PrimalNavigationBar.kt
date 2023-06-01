package net.primal.android.core.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
 import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Discuss
import net.primal.android.core.compose.icons.primaliconpack.Messages
import net.primal.android.core.compose.icons.primaliconpack.Notifications
import net.primal.android.core.compose.icons.primaliconpack.Read
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.theme.AppTheme

@Composable
fun PrimalNavigationBar(
    modifier: Modifier = Modifier,
    activeDestination: PrimalTopLevelDestination,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        tonalElevation = 0.dp,
    ) {
        PrimalTopLevelDestination.values().forEach {
            PrimalNavigationBarItem(
                primaryDestination = it,
                activeDestination = activeDestination,
                onClick = {
                    if (activeDestination != it) {
                        onTopLevelDestinationChanged(it)
                    }
                },
            )
        }
    }
}

@Composable
private fun RowScope.PrimalNavigationBarItem(
    primaryDestination: PrimalTopLevelDestination,
    activeDestination: PrimalTopLevelDestination,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = primaryDestination == activeDestination,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = primaryDestination.imageVector(),
                contentDescription = primaryDestination.label(),
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = AppTheme.colorScheme.surface,
            selectedIconColor = AppTheme.colorScheme.onSurface,
            unselectedIconColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        ),
    )
}

enum class PrimalTopLevelDestination {
    Feed, Read, Explore, Messages, Notifications
}

private fun PrimalTopLevelDestination.imageVector(): ImageVector {
    return when (this) {
        PrimalTopLevelDestination.Feed -> PrimalIcons.Discuss
        PrimalTopLevelDestination.Read -> PrimalIcons.Read
        PrimalTopLevelDestination.Explore -> PrimalIcons.Search
        PrimalTopLevelDestination.Messages -> PrimalIcons.Messages
        PrimalTopLevelDestination.Notifications -> PrimalIcons.Notifications
    }
}

@Composable
private fun PrimalTopLevelDestination.label(): String {
    return when (this) {
        PrimalTopLevelDestination.Feed -> stringResource(id = R.string.primary_destination_feed_label)
        PrimalTopLevelDestination.Read -> stringResource(id = R.string.primary_destination_read_label)
        PrimalTopLevelDestination.Explore -> stringResource(id = R.string.primary_destination_explore_label)
        PrimalTopLevelDestination.Messages -> stringResource(id = R.string.primary_destination_messages_label)
        PrimalTopLevelDestination.Notifications -> stringResource(id = R.string.primary_destination_notifications_label)
    }
}
