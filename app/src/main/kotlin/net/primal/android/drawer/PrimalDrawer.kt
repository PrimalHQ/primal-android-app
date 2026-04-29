package net.primal.android.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalOverlayBottomBar
import net.primal.android.core.compose.PrimalOverlayCloseButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.DrawerBookmarks
import net.primal.android.core.compose.icons.primaliconpack.DrawerMessages
import net.primal.android.core.compose.icons.primaliconpack.DrawerPremium
import net.primal.android.core.compose.icons.primaliconpack.DrawerProfile
import net.primal.android.core.compose.icons.primaliconpack.DrawerSettings
import net.primal.android.core.compose.icons.primaliconpack.DrawerSignOut
import net.primal.android.core.compose.icons.primaliconpack.QrCode
import net.primal.android.core.compose.icons.primaliconpack.RemoteLogin
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.drawer.multiaccount.AccountSwitcher
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.UserAccount

@Composable
fun PrimalDrawer(
    onDismiss: () -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onQrCodeClick: () -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
) {
    val viewModel = hiltViewModel<PrimalDrawerViewModel>()

    val uiState = viewModel.state.collectAsState()

    PrimalDrawer(
        state = uiState.value,
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        onDismiss = onDismiss,
        onDrawerDestinationClick = {
            when (it) {
                is DrawerScreenDestination.SignOut -> Unit
                else -> onDismiss()
            }
            onDrawerDestinationClick(it)
        },
        onQrCodeClick = {
            onDismiss()
            onQrCodeClick()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrimalDrawer(
    state: PrimalDrawerContract.UiState,
    onDismiss: () -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onQrCodeClick: () -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .navigationBarsPadding()
            .padding(top = 16.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                DrawerHeader(
                    userAccount = state.activeUserAccount,
                    onQrCodeClick = onQrCodeClick,
                    legendaryCustomization = state.legendaryCustomization,
                )

                DrawerMenu(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(top = 32.dp),
                    state = state,
                    showPremiumBadge = state.showPremiumBadge,
                    onDrawerDestinationClick = onDrawerDestinationClick,
                )
            }

            AccountSwitcher(
                callbacks = accountSwitcherCallbacks,
                onLogoutClick = { onDrawerDestinationClick(DrawerScreenDestination.SignOut(it)) },
            )
        }

        PrimalOverlayBottomBar(
            trailing = { PrimalOverlayCloseButton(onClick = onDismiss) },
        )
    }
}

@Composable
private fun DrawerHeader(
    userAccount: UserAccount?,
    legendaryCustomization: LegendaryCustomization?,
    onQrCodeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NostrUserText(
                profileId = userAccount?.pubkey,
                displayName = userAccount?.authorDisplayName ?: "",
                internetIdentifier = userAccount?.internetIdentifier,
                internetIdentifierBadgeSize = 24.dp,
                legendaryCustomization = legendaryCustomization,
            )

            IconButton(onClick = onQrCodeClick) {
                Icon(
                    imageVector = PrimalIcons.QrCode,
                    contentDescription = stringResource(id = R.string.accessibility_qr_code),
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }

        val formattedIdentifier = userAccount?.internetIdentifier?.formatNip05Identifier()
        if (!formattedIdentifier.isNullOrBlank()) {
            Text(
                text = formattedIdentifier,
                style = AppTheme.typography.labelLarge,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }

        Text(
            text = buildStatsAnnotatedString(
                followersCount = userAccount?.followersCount,
                followingCount = userAccount?.followingCount,
            ),
            style = AppTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun buildStatsAnnotatedString(followingCount: Int?, followersCount: Int?): AnnotatedString {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    return buildAnnotatedString {
        append(
            AnnotatedString(
                text = followingCount?.let { numberFormat.format(it) } ?: "-",
                spanStyle = SpanStyle(
                    color = AppTheme.extraColorScheme.onBrand,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
        append(
            AnnotatedString(
                text = " " + stringResource(id = R.string.drawer_following_suffix),
                spanStyle = SpanStyle(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
        append("   ")
        append(
            AnnotatedString(
                text = followersCount?.let { numberFormat.format(it) } ?: "-",
                spanStyle = SpanStyle(
                    color = AppTheme.extraColorScheme.onBrand,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
        append(
            AnnotatedString(
                text = " " + stringResource(id = R.string.drawer_followers_suffix),
                spanStyle = SpanStyle(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
    }
}

@Composable
private fun DrawerMenu(
    modifier: Modifier,
    state: PrimalDrawerContract.UiState,
    showPremiumBadge: Boolean,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        items(
            items = state.menuItems,
            key = { it.toString() },
        ) { item ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    leadingIconColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    headlineColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                ),
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        onDrawerDestinationClick(item)
                    },
                ),
                leadingContent = {
                    Icon(
                        modifier = Modifier.padding(start = 8.dp),
                        imageVector = item.icon(),
                        contentDescription = item.label(),
                        tint = if (isPressed) {
                            AppTheme.colorScheme.onSurface
                        } else {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt2
                        },
                    )
                },
                headlineContent = {
                    val isPremiumWithRequiredBadge = (item is DrawerScreenDestination.Premium && showPremiumBadge)
                    val isMessagesWithUnreadCount = item is DrawerScreenDestination.Messages &&
                        state.badges.unreadMessagesCount > 0

                    BadgedBox(
                        badge = {
                            if (isMessagesWithUnreadCount || isPremiumWithRequiredBadge) {
                                Badge(
                                    modifier = Modifier
                                        .size(size = 8.dp)
                                        .offset(x = 16.dp, y = (-8).dp),
                                    containerColor = AppTheme.colorScheme.primary,
                                )
                            }
                        },
                    ) {
                        Text(
                            text = item.label(),
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(top = (3.5).dp),
                            style = AppTheme.typography.titleLarge,
                            color = if (isPressed) {
                                AppTheme.colorScheme.onSurface
                            } else {
                                AppTheme.extraColorScheme.onSurfaceVariantAlt2
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 20.sp,
                        )
                    }
                },
            )
        }
    }
}

sealed class DrawerScreenDestination {
    data class Profile(val userId: String) : DrawerScreenDestination()
    data class Premium(val hasPremium: Boolean) : DrawerScreenDestination()
    data object Messages : DrawerScreenDestination()
    data class Bookmarks(val userId: String) : DrawerScreenDestination()
    data object ScanCode : DrawerScreenDestination()
    data object RemoteLogin : DrawerScreenDestination()
    data object Settings : DrawerScreenDestination()
    data class SignOut(val userId: String) : DrawerScreenDestination()
}

@Composable
private fun DrawerScreenDestination.label(): String {
    return when (this) {
        is DrawerScreenDestination.Profile -> stringResource(R.string.drawer_destination_profile)
        is DrawerScreenDestination.Premium -> stringResource(id = R.string.drawer_destination_premium)
        DrawerScreenDestination.Messages -> stringResource(R.string.drawer_destination_messages)
        is DrawerScreenDestination.Bookmarks -> stringResource(R.string.drawer_destination_bookmarks)
        DrawerScreenDestination.RemoteLogin -> stringResource(id = R.string.drawer_destination_remote_login)
        DrawerScreenDestination.ScanCode -> stringResource(id = R.string.drawer_destination_scan_code)
        DrawerScreenDestination.Settings -> stringResource(R.string.drawer_destination_settings)
        is DrawerScreenDestination.SignOut -> stringResource(R.string.drawer_destination_sign_out)
    }
}

@Composable
private fun DrawerScreenDestination.icon(): ImageVector {
    return when (this) {
        is DrawerScreenDestination.Profile -> PrimalIcons.DrawerProfile
        is DrawerScreenDestination.Premium -> PrimalIcons.DrawerPremium
        DrawerScreenDestination.Messages -> PrimalIcons.DrawerMessages
        is DrawerScreenDestination.Bookmarks -> PrimalIcons.DrawerBookmarks
        DrawerScreenDestination.RemoteLogin -> PrimalIcons.RemoteLogin
        DrawerScreenDestination.ScanCode -> PrimalIcons.QrCode
        DrawerScreenDestination.Settings -> PrimalIcons.DrawerSettings
        is DrawerScreenDestination.SignOut -> PrimalIcons.DrawerSignOut
    }
}

@Preview
@Composable
fun PrimalDrawerPreview() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        PrimalDrawer(
            state = PrimalDrawerContract.UiState(
                menuItems = listOf(
                    DrawerScreenDestination.Profile(userId = "none"),
                    DrawerScreenDestination.Messages,
                    DrawerScreenDestination.Bookmarks(userId = "none"),
                    DrawerScreenDestination.ScanCode,
                    DrawerScreenDestination.Settings,
                    DrawerScreenDestination.SignOut(userId = "none"),
                ),
            ),
            onDismiss = {},
            onDrawerDestinationClick = {},
            onQrCodeClick = {},
            accountSwitcherCallbacks = AccountSwitcherCallbacks(
                onActiveAccountChanged = {},
                onAddExistingAccountClick = {},
                onCreateNewAccountClick = {},
            ),
        )
    }
}
