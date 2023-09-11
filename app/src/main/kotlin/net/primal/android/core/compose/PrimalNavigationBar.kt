package net.primal.android.core.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.Badges

@Composable
fun PrimalNavigationBar(
    modifier: Modifier = Modifier,
    activeDestination: PrimalTopLevelDestination,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onActiveDestinationClick: (() -> Unit)? = null,
    badges: Badges = Badges(),
) {
    NavigationBar(
        modifier = modifier,
        tonalElevation = 0.dp,
    ) {
        val badgesMap = mapOf(
            Pair(PrimalTopLevelDestination.Messages, badges.messages),
            Pair(PrimalTopLevelDestination.Notifications, badges.notifications)
        )
        PrimalTopLevelDestination.values().forEach {
            PrimalNavigationBarItem(
                primaryDestination = it,
                activeDestination = activeDestination,
                onClick = {
                    if (activeDestination != it) {
                        onTopLevelDestinationChanged(it)
                    } else {
                        onActiveDestinationClick?.invoke()
                    }
                },
                badge = badgesMap.getOrDefault(it, 0),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.PrimalNavigationBarItem(
    primaryDestination: PrimalTopLevelDestination,
    activeDestination: PrimalTopLevelDestination,
    onClick: () -> Unit,
    badge: Int = 0,
) {
    NavigationBarItem(
        selected = primaryDestination == activeDestination,
        onClick = onClick,
        icon = {
            BadgedBox(badge = { if (badge > 0) Badge() }) {
                Icon(
                    imageVector = primaryDestination.imageVector(),
                    contentDescription = primaryDestination.label(),
                )
            }

        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = AppTheme.colorScheme.surface,
            selectedIconColor = AppTheme.colorScheme.onSurface,
            unselectedIconColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        ),
    )
}

enum class PrimalTopLevelDestination {
    Feed, Explore, Messages, Notifications
}

private fun PrimalTopLevelDestination.imageVector(): ImageVector {
    return when (this) {
        PrimalTopLevelDestination.Feed -> PrimalIcons.Discuss
        PrimalTopLevelDestination.Explore -> PrimalIcons.Search
        PrimalTopLevelDestination.Messages -> PrimalIcons.Messages
        PrimalTopLevelDestination.Notifications -> PrimalIcons.Notifications
    }
}

@Composable
private fun PrimalTopLevelDestination.label(): String {
    return when (this) {
        PrimalTopLevelDestination.Feed -> stringResource(id = R.string.primary_destination_feed_label)
        PrimalTopLevelDestination.Explore -> stringResource(id = R.string.primary_destination_explore_label)
        PrimalTopLevelDestination.Messages -> stringResource(id = R.string.primary_destination_messages_label)
        PrimalTopLevelDestination.Notifications -> stringResource(id = R.string.primary_destination_notifications_label)
    }
}
