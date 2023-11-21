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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.UserAccount

@Composable
fun PrimalDrawer(drawerState: DrawerState, onDrawerDestinationClick: (DrawerScreenDestination) -> Unit) {
    val uiScope = rememberCoroutineScope()
    val viewModel = hiltViewModel<PrimalDrawerViewModel>()

    BackHandler(enabled = drawerState.isOpen) {
        uiScope.launch { drawerState.close() }
    }

    PrimalDrawer(
        viewModel = viewModel,
        onDrawerDestinationClick = {
            when (it) {
                DrawerScreenDestination.SignOut -> Unit
                else -> uiScope.launch { drawerState.close() }
            }
            onDrawerDestinationClick(it)
        },
    )
}

@Composable
fun PrimalDrawer(viewModel: PrimalDrawerViewModel, onDrawerDestinationClick: (DrawerScreenDestination) -> Unit) {
    val uiState = viewModel.state.collectAsState()

    PrimalDrawer(
        state = uiState.value,
        onDrawerDestinationClick = onDrawerDestinationClick,
        eventPublisher = {
            viewModel.setEvent(it)
        },
    )
}

@Composable
fun PrimalDrawer(
    state: PrimalDrawerContract.UiState,
    eventPublisher: (PrimalDrawerContract.UiEvent) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
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
            )

            DrawerMenu(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(vertical = 32.dp),
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
private fun DrawerHeader(userAccount: UserAccount?) {
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
            Icon(imageVector = PrimalIcons.QrCode, contentDescription = null)
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
private fun DrawerMenu(modifier: Modifier, onDrawerDestinationClick: (DrawerScreenDestination) -> Unit) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        items(
            items = DrawerScreenDestination.values(),
            key = { it.name },
        ) {
            ListItem(
                modifier = Modifier.clickable {
                    onDrawerDestinationClick(it)
                },
                headlineContent = {
                    Text(
                        text = it.label().uppercase(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colorScheme.onSurfaceVariant,
                    )
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
            Icon(imageVector = iconVector, contentDescription = null)
        }
    }
}

enum class DrawerScreenDestination {
    Profile,
    Settings,
    SignOut,
}

@Composable
private fun DrawerScreenDestination.label(): String {
    return when (this) {
        DrawerScreenDestination.Profile -> stringResource(id = R.string.drawer_destination_profile)
        DrawerScreenDestination.Settings -> stringResource(
            id = R.string.drawer_destination_settings,
        )
        DrawerScreenDestination.SignOut -> stringResource(id = R.string.drawer_destination_sign_out)
    }
}

@Preview
@Composable
fun PrimalDrawerPreview() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        PrimalDrawer(
            state = PrimalDrawerContract.UiState(),
            eventPublisher = {},
            onDrawerDestinationClick = {},
        )
    }
}
