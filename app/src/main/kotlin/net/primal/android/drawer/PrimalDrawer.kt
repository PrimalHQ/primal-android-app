package net.primal.android.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.DarkMode
import net.primal.android.core.compose.icons.primaliconpack.LightMode
import net.primal.android.core.compose.icons.primaliconpack.QrCode
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.Badges
import net.primal.android.user.domain.UserAccount

@Composable
fun PrimalDrawer(
    drawerState: DrawerState,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onQrCodeClick: () -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val viewModel = hiltViewModel<PrimalDrawerViewModel>()

    BackHandler(enabled = drawerState.isOpen) {
        uiScope.launch { drawerState.close() }
    }

    val uiState = viewModel.state.collectAsState()

    PrimalDrawer(
        state = uiState.value,
        onDrawerDestinationClick = {
            when (it) {
                DrawerScreenDestination.SignOut -> Unit
                else -> uiScope.launch { drawerState.close() }
            }
            onDrawerDestinationClick(it)
        },
        eventPublisher = { viewModel.setEvent(it) },
        onQrCodeClick = {
            uiScope.launch { drawerState.close() }
            onQrCodeClick()
        },
    )
}

@Composable
fun PrimalDrawer(
    state: PrimalDrawerContract.UiState,
    eventPublisher: (PrimalDrawerContract.UiEvent) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onQrCodeClick: () -> Unit,
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    Surface {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .navigationBarsPadding()
                .width(300.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            DrawerHeader(
                userAccount = state.activeUserAccount,
                onQrCodeClick = onQrCodeClick,
            )

            DrawerMenu(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(vertical = 32.dp),
                menuItems = state.menuItems,
                badges = state.badges,
                onDrawerDestinationClick = onDrawerDestinationClick,
            )

            DrawerFooter(
                onThemeSwitch = {
                    eventPublisher(
                        PrimalDrawerContract.UiEvent.ThemeSwitchClick(
                            isSystemInDarkTheme = isSystemInDarkTheme,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun DrawerHeader(userAccount: UserAccount?, onQrCodeClick: () -> Unit) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val startGuideline = createGuidelineFromStart(24.dp)
        val (avatarRef, usernameRef, iconRef, identifierRef, statsRef) = createRefs()

        AvatarThumbnail(
            modifier = Modifier.constrainAs(avatarRef) {
                start.linkTo(startGuideline)
                top.linkTo(parent.top, margin = 16.dp)
            },
            avatarSize = 52.dp,
            avatarCdnImage = userAccount?.avatarCdnImage,
        )

        NostrUserText(
            displayName = userAccount?.authorDisplayName ?: "",
            internetIdentifier = userAccount?.internetIdentifier,
            internetIdentifierBadgeSize = 24.dp,
            modifier = Modifier.constrainAs(usernameRef) {
                start.linkTo(startGuideline)
                top.linkTo(avatarRef.bottom, margin = 16.dp)
                width = Dimension.preferredValue(220.dp)
            },
        )

        IconButton(
            modifier = Modifier.constrainAs(iconRef) {
                centerVerticallyTo(usernameRef)
                start.linkTo(usernameRef.end)
                width = Dimension.preferredWrapContent
            },
            onClick = {
            },
        ) {
            Icon(
                modifier = Modifier.clickable { onQrCodeClick() },
                imageVector = PrimalIcons.QrCode,
                contentDescription = stringResource(id = R.string.accessibility_qr_code),
            )
        }

        Text(
            text = userAccount?.internetIdentifier?.formatNip05Identifier() ?: "",
            style = AppTheme.typography.labelLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            modifier = Modifier.constrainAs(identifierRef) {
                start.linkTo(startGuideline)
                top.linkTo(usernameRef.bottom, margin = 8.dp)
            },
        )

        val statsAnnotatedString = buildAnnotatedString {
            append(
                AnnotatedString(
                    text = userAccount?.followingCount?.let { numberFormat.format(it) } ?: "-",
                    spanStyle = SpanStyle(
                        color = AppTheme.colorScheme.onSurfaceVariant,
                        fontStyle = AppTheme.typography.labelLarge.fontStyle,
                    ),
                ),
            )
            append(
                AnnotatedString(
                    text = " " + stringResource(id = R.string.drawer_following_suffix),
                    spanStyle = SpanStyle(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        fontStyle = AppTheme.typography.labelLarge.fontStyle,
                    ),
                ),
            )
            append("   ")
            append(
                AnnotatedString(
                    text = userAccount?.followersCount?.let { numberFormat.format(it) } ?: "-",
                    spanStyle = SpanStyle(
                        color = AppTheme.colorScheme.onSurfaceVariant,
                        fontStyle = AppTheme.typography.labelLarge.fontStyle,
                    ),
                ),
            )
            append(
                AnnotatedString(
                    text = " " + stringResource(id = R.string.drawer_followers_suffix),
                    spanStyle = SpanStyle(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        fontStyle = AppTheme.typography.labelLarge.fontStyle,
                    ),
                ),
            )
        }
        Text(
            text = statsAnnotatedString,
            style = AppTheme.typography.labelLarge,
            modifier = Modifier.constrainAs(statsRef) {
                start.linkTo(startGuideline)
                top.linkTo(identifierRef.bottom, margin = 16.dp)
            },
        )
    }
}

@Composable
private fun DrawerMenu(
    modifier: Modifier,
    menuItems: List<DrawerScreenDestination>,
    badges: Badges,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        items(
            items = menuItems,
            key = { it.toString() },
        ) { item ->
            ListItem(
                modifier = Modifier.clickable {
                    onDrawerDestinationClick(item)
                },
                headlineContent = {
                    BadgedBox(
                        badge = {
                            if (item is DrawerScreenDestination.Messages && badges.unreadMessagesCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .size(size = 10.dp)
                                        .offset(x = 8.dp),
                                    containerColor = AppTheme.colorScheme.primary,
                                )
                            }
                        },
                    ) {
                        Text(
                            text = item.label().uppercase(),
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun DrawerFooter(onThemeSwitch: () -> Unit) {
    Box(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
    ) {
        val isDarkTheme = AppTheme.colorScheme.surface.luminance() < 0.5f
        val iconVector = if (isDarkTheme) PrimalIcons.DarkMode else PrimalIcons.LightMode
        IconButton(
            onClick = onThemeSwitch,
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = stringResource(id = R.string.accessibility_toggle_between_dark_and_light_mode),
            )
        }
    }
}

sealed class DrawerScreenDestination {
    data object Profile : DrawerScreenDestination()
    data class Premium(val hasPremium: Boolean) : DrawerScreenDestination()
    data object DemoManagePremium : DrawerScreenDestination()
    data object Messages : DrawerScreenDestination()
    data class Bookmarks(val userId: String) : DrawerScreenDestination()
    data object Settings : DrawerScreenDestination()
    data object SignOut : DrawerScreenDestination()
}

@Composable
private fun DrawerScreenDestination.label(): String {
    return when (this) {
        DrawerScreenDestination.Profile -> stringResource(R.string.drawer_destination_profile)
        is DrawerScreenDestination.Premium -> stringResource(id = R.string.drawer_destination_premium)
        DrawerScreenDestination.Messages -> stringResource(R.string.drawer_destination_messages)
        DrawerScreenDestination.DemoManagePremium -> "Premium Manage"
        is DrawerScreenDestination.Bookmarks -> stringResource(R.string.drawer_destination_bookmarks)
        DrawerScreenDestination.Settings -> stringResource(R.string.drawer_destination_settings)
        DrawerScreenDestination.SignOut -> stringResource(R.string.drawer_destination_sign_out)
    }
}

@Preview
@Composable
fun PrimalDrawerPreview() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        PrimalDrawer(
            state = PrimalDrawerContract.UiState(
                menuItems = listOf(
                    DrawerScreenDestination.Profile,
                    DrawerScreenDestination.Messages,
                    DrawerScreenDestination.Bookmarks(userId = "none"),
                    DrawerScreenDestination.Settings,
                    DrawerScreenDestination.SignOut,
                ),
            ),
            eventPublisher = {},
            onDrawerDestinationClick = {},
            onQrCodeClick = {},
        )
    }
}
