package net.primal.android.core.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ExploreFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedPickerFilled
import net.primal.android.core.compose.icons.primaliconpack.LongReadFilled
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.core.compose.icons.primaliconpack.NotificationsFilled
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.Badges

val NavigationBarFullHeightDp = 64.dp

@Composable
fun PrimalNavigationBar(
    modifier: Modifier = Modifier,
    activeDestination: PrimalTopLevelDestination,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onActiveDestinationClick: (() -> Unit)? = null,
    badges: Badges = Badges(),
) {
    val badgesMap = mapOf(
        Pair(PrimalTopLevelDestination.Alerts, badges.unreadNotificationsCount),
    )

    Surface(color = Color.Transparent) {
        Column(modifier = modifier) {
            PrimalDivider()

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = AppTheme.colorScheme.surface)
                    .height(NavigationBarFullHeightDp),
            ) {
                val horizontalPadding = 12.dp
                val topPadding = 4.dp
                val pillWidth = 72.dp
                val itemWidth = (maxWidth - horizontalPadding * 2) / PrimalTopLevelDestination.entries.size
                val selectedIndex = PrimalTopLevelDestination.entries.indexOf(activeDestination)

                val pillOffset by animateDpAsState(
                    targetValue = horizontalPadding + itemWidth * selectedIndex + (itemWidth - pillWidth) / 2,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "pillOffset",
                )

                Box(
                    modifier = Modifier
                        .offset(x = pillOffset, y = topPadding)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .width(pillWidth)
                            .background(
                                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                                shape = CircleShape,
                            ),
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = topPadding)
                        .padding(horizontal = horizontalPadding)
                        .fillMaxSize(),
                ) {
                    PrimalTopLevelDestination.entries.forEach { destination ->
                        PrimalNavigationBarItem(
                            modifier = Modifier.weight(1f),
                            destination = destination,
                            selected = destination == activeDestination,
                            badge = badgesMap.getOrDefault(destination, 0),
                            onClick = {
                                if (activeDestination != destination) {
                                    onTopLevelDestinationChanged(destination)
                                } else {
                                    onActiveDestinationClick?.invoke()
                                }
                            },
                        )
                    }
                }
            }

            val navBarHeight = with(LocalDensity.current) {
                WindowInsets.navigationBars.getBottom(this).toDp()
            }
            Spacer(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .height(navBarHeight),
            )
        }
    }
}

@Composable
private fun PrimalNavigationBarItem(
    modifier: Modifier = Modifier,
    destination: PrimalTopLevelDestination,
    selected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit,
) {
    val tint = if (selected) {
        AppTheme.colorScheme.onSurface
    } else {
        AppTheme.extraColorScheme.onSurfaceVariantAlt3
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .padding(top = 4.dp)
            .clickable(indication = null, interactionSource = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BadgedBox(
            badge = {
                if (badge > 0) {
                    Badge(containerColor = AppTheme.colorScheme.primary)
                }
            },
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = destination.imageVector(),
                contentDescription = destination.label(),
                tint = tint,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = destination.label(),
            style = AppTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 10.sp),
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

enum class PrimalTopLevelDestination {
    Feeds,
    Reads,
    Wallet,
    Alerts,
    Explore,
}

private fun PrimalTopLevelDestination.imageVector(): ImageVector {
    return when (this) {
        PrimalTopLevelDestination.Feeds -> PrimalIcons.FeedPickerFilled
        PrimalTopLevelDestination.Reads -> PrimalIcons.LongReadFilled
        PrimalTopLevelDestination.Wallet -> PrimalIcons.NavWalletBoltFilled
        PrimalTopLevelDestination.Alerts -> PrimalIcons.NotificationsFilled
        PrimalTopLevelDestination.Explore -> PrimalIcons.ExploreFilled
    }
}

@Composable
private fun PrimalTopLevelDestination.label(): String {
    return when (this) {
        PrimalTopLevelDestination.Feeds -> stringResource(id = R.string.primary_destination_feed_label)
        PrimalTopLevelDestination.Reads -> stringResource(id = R.string.primary_destination_reads_label)
        PrimalTopLevelDestination.Wallet -> stringResource(id = R.string.primary_destination_wallet_label)
        PrimalTopLevelDestination.Alerts -> stringResource(id = R.string.primary_destination_notifications_label)
        PrimalTopLevelDestination.Explore -> stringResource(id = R.string.primary_destination_explore_label)
    }
}

@Preview
@Composable
fun PreviewNavigationBar() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Midnight) {
        Surface(modifier = Modifier.wrapContentSize()) {
            PrimalNavigationBar(
                activeDestination = PrimalTopLevelDestination.Feeds,
                onTopLevelDestinationChanged = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewNavigationBarReads() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Midnight) {
        Surface(modifier = Modifier.wrapContentSize()) {
            PrimalNavigationBar(
                activeDestination = PrimalTopLevelDestination.Reads,
                badges = Badges(unreadNotificationsCount = 1),
                onTopLevelDestinationChanged = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewNavigationBarWallet() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Midnight) {
        Surface(modifier = Modifier.wrapContentSize()) {
            PrimalNavigationBar(
                activeDestination = PrimalTopLevelDestination.Wallet,
                badges = Badges(unreadNotificationsCount = 1),
                onTopLevelDestinationChanged = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewNavigationBarAlerts() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Midnight) {
        Surface(modifier = Modifier.wrapContentSize()) {
            PrimalNavigationBar(
                activeDestination = PrimalTopLevelDestination.Alerts,
                badges = Badges(unreadNotificationsCount = 1),
                onTopLevelDestinationChanged = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewNavigationBarExplore() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Midnight) {
        Surface(modifier = Modifier.wrapContentSize()) {
            PrimalNavigationBar(
                activeDestination = PrimalTopLevelDestination.Explore,
                badges = Badges(unreadNotificationsCount = 1),
                onTopLevelDestinationChanged = {},
            )
        }
    }
}
