package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.foundation.ClickDebounce
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NavHome
import net.primal.android.core.compose.icons.primaliconpack.NavHomeFilled
import net.primal.android.core.compose.icons.primaliconpack.NavMessages
import net.primal.android.core.compose.icons.primaliconpack.NavMessagesFilled
import net.primal.android.core.compose.icons.primaliconpack.NavNotifications
import net.primal.android.core.compose.icons.primaliconpack.NavNotificationsFilled
import net.primal.android.core.compose.icons.primaliconpack.NavSearch
import net.primal.android.core.compose.icons.primaliconpack.NavSearchFilled
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBolt
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.user.domain.Badges

private val NavigationBarFullHeightDp = 64.dp
private val NavigationBarBoltCircleSizeDp = NavigationBarFullHeightDp
private val NavigationBarVisibleHeightDp = 56.dp
private val NavigationBarBoltBorderSpaceDp = 6.dp
private val NavigationBarBorderWidthDp = 0.5.dp

@Composable
private fun navigationBarColors() =
    NavigationBarItemDefaults.colors(
        indicatorColor = AppTheme.colorScheme.surface,
        selectedIconColor = AppTheme.colorScheme.onSurface,
        unselectedIconColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    )

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
            Pair(PrimalTopLevelDestination.Messages, badges.unreadMessagesCount),
            Pair(PrimalTopLevelDestination.Notifications, badges.unreadNotificationsCount),
        )
        PrimalTopLevelDestination.entries.forEach {
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

@Composable
fun PrimalNavigationBarLightningBolt(
    modifier: Modifier = Modifier,
    activeDestination: PrimalTopLevelDestination,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onActiveDestinationClick: (() -> Unit)? = null,
    badges: Badges = Badges(),
) {
    val clickDebounce by remember { mutableStateOf(ClickDebounce()) }
    val badgesMap = mapOf(
        Pair(PrimalTopLevelDestination.Messages, badges.unreadMessagesCount),
        Pair(PrimalTopLevelDestination.Notifications, badges.unreadNotificationsCount),
    )

    Surface(color = Color.Transparent) {
        BoxWithConstraints(
            modifier = modifier.height(NavigationBarFullHeightDp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Spacer(
                modifier = Modifier
                    .size(NavigationBarBoltCircleSizeDp)
                    .border(
                        width = NavigationBarBorderWidthDp,
                        color = AppTheme.colorScheme.outline,
                        shape = CircleShape,
                    ),
            )

            Column {
                PrimalDivider(thickness = NavigationBarBorderWidthDp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = NavigationBarDefaults.containerColor)
                        .height(NavigationBarVisibleHeightDp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    NavItemDestination(
                        destination = PrimalTopLevelDestination.Home,
                        activeDestination = activeDestination,
                        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
                        onActiveDestinationClick = onActiveDestinationClick,
                        badgesMap = badgesMap,
                    )

                    NavItemDestination(
                        destination = PrimalTopLevelDestination.Explore,
                        activeDestination = activeDestination,
                        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
                        onActiveDestinationClick = onActiveDestinationClick,
                        badgesMap = badgesMap,
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                    ) {}

                    NavItemDestination(
                        destination = PrimalTopLevelDestination.Notifications,
                        activeDestination = activeDestination,
                        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
                        onActiveDestinationClick = onActiveDestinationClick,
                        badgesMap = badgesMap,
                    )

                    NavItemDestination(
                        destination = PrimalTopLevelDestination.Messages,
                        activeDestination = activeDestination,
                        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
                        onActiveDestinationClick = onActiveDestinationClick,
                        badgesMap = badgesMap,
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .size(NavigationBarBoltCircleSizeDp - NavigationBarBorderWidthDp)
                    .background(
                        color = AppTheme.colorScheme.surface,
                        shape = CircleShape,
                    ),
            )

            Box(
                modifier = Modifier
                    .size(NavigationBarBoltCircleSizeDp - NavigationBarBoltBorderSpaceDp)
                    .background(
                        color = if (activeDestination == PrimalTopLevelDestination.Wallet) {
                            AppTheme.colorScheme.outline
                        } else {
                            AppTheme.extraColorScheme.surfaceVariantAlt1
                        },
                        shape = CircleShape,
                    )
                    .clip(CircleShape)
                    .clickable {
                        clickDebounce.processEvent {
                            onTopLevelDestinationChanged(PrimalTopLevelDestination.Wallet)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                val selected = activeDestination == PrimalTopLevelDestination.Wallet
                val imageVector = if (selected) {
                    PrimalTopLevelDestination.Wallet.imageVectorSelected()
                } else {
                    PrimalTopLevelDestination.Wallet.imageVector()
                }

                val tint = if (selected) {
                    AppTheme.colorScheme.onSurface
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt3
                }

                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = tint,
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavItemDestination(
    destination: PrimalTopLevelDestination,
    activeDestination: PrimalTopLevelDestination,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onActiveDestinationClick: (() -> Unit)?,
    badgesMap: Map<PrimalTopLevelDestination, Int>,
) {
    PrimalNavigationBarItem(
        primaryDestination = destination,
        activeDestination = activeDestination,
        onClick = {
            if (activeDestination != destination) {
                onTopLevelDestinationChanged(destination)
            } else {
                onActiveDestinationClick?.invoke()
            }
        },
        badge = badgesMap.getOrDefault(destination, 0),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.PrimalNavigationBarItem(
    primaryDestination: PrimalTopLevelDestination,
    activeDestination: PrimalTopLevelDestination,
    onClick: () -> Unit,
    badge: Int = 0,
) {
    val selected = primaryDestination == activeDestination
    val clickDebounce by remember { mutableStateOf(ClickDebounce()) }
    NavigationBarItem(
        selected = selected,
        onClick = { clickDebounce.processEvent(onClick) },
        icon = {
            BadgedBox(
                badge = {
                    if (badge > 0) {
                        Badge(containerColor = AppTheme.colorScheme.primary)
                    }
                },
            ) {
                Icon(
                    imageVector = if (selected) {
                        primaryDestination.imageVectorSelected()
                    } else {
                        primaryDestination.imageVector()
                    },
                    contentDescription = primaryDestination.label(),
                )
            }
        },
        colors = navigationBarColors(),
    )
}

enum class PrimalTopLevelDestination {
    Home,
    Explore,
    Wallet,
    Notifications,
    Messages,
}

private fun PrimalTopLevelDestination.imageVector(): ImageVector {
    return when (this) {
        PrimalTopLevelDestination.Home -> PrimalIcons.NavHome
        PrimalTopLevelDestination.Explore -> PrimalIcons.NavSearch
        PrimalTopLevelDestination.Wallet -> PrimalIcons.NavWalletBolt
        PrimalTopLevelDestination.Messages -> PrimalIcons.NavMessages
        PrimalTopLevelDestination.Notifications -> PrimalIcons.NavNotifications
    }
}

private fun PrimalTopLevelDestination.imageVectorSelected(): ImageVector {
    return when (this) {
        PrimalTopLevelDestination.Home -> PrimalIcons.NavHomeFilled
        PrimalTopLevelDestination.Explore -> PrimalIcons.NavSearchFilled
        PrimalTopLevelDestination.Wallet -> PrimalIcons.NavWalletBoltFilled
        PrimalTopLevelDestination.Messages -> PrimalIcons.NavMessagesFilled
        PrimalTopLevelDestination.Notifications -> PrimalIcons.NavNotificationsFilled
    }
}

@Composable
private fun PrimalTopLevelDestination.label(): String {
    return when (this) {
        PrimalTopLevelDestination.Home -> stringResource(id = R.string.primary_destination_feed_label)
        PrimalTopLevelDestination.Explore -> stringResource(id = R.string.primary_destination_explore_label)
        PrimalTopLevelDestination.Wallet -> stringResource(id = R.string.primary_destination_wallet_label)
        PrimalTopLevelDestination.Messages -> stringResource(id = R.string.primary_destination_messages_label)
        PrimalTopLevelDestination.Notifications -> stringResource(id = R.string.primary_destination_notifications_label)
    }
}

@Preview
@Composable
fun PreviewNavigationBar() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(
            modifier = Modifier.wrapContentSize(),
        ) {
            PrimalNavigationBar(
                modifier = Modifier,
                activeDestination = PrimalTopLevelDestination.Home,
                onTopLevelDestinationChanged = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewNavigationBarLightningBolt() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(
            modifier = Modifier.wrapContentSize(),
        ) {
            PrimalNavigationBarLightningBolt(
                modifier = Modifier,
                activeDestination = PrimalTopLevelDestination.Home,
                onTopLevelDestinationChanged = {},
            )
        }
    }
}
