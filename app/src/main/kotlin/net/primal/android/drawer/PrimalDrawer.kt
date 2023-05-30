package net.primal.android.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.DarkMode
import net.primal.android.core.compose.icons.primaliconpack.QrCode
import net.primal.android.theme.PrimalTheme


@Composable
fun PrimalDrawer(
    drawerState: DrawerState,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
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
fun PrimalDrawer(
    viewModel: PrimalDrawerViewModel,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    PrimalDrawer(
        state = uiState.value,
        onDrawerDestinationClick = onDrawerDestinationClick
    )
}

@Composable
fun PrimalDrawer(
    state: PrimalDrawerContract.UiState,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    Surface {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .navigationBarsPadding()
                .width(300.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            DrawerHeader()

            DrawerMenu(
                modifier = Modifier.weight(1.0f).padding(vertical = 32.dp),
                onDrawerDestinationClick = onDrawerDestinationClick,
            )

            DrawerFooter(
                onThemeSwitch = {

                }
            )
        }
    }
}

@Composable
private fun DrawerHeader() {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val startGuideline = createGuidelineFromStart(24.dp)
        val (avatarRef, usernameRef, iconRef, identifierRef, statsRef) = createRefs()

        AvatarThumbnailListItemImage(
            source = "https://i.imgur.com/Z8dpmvc.png",
            modifier = Modifier.constrainAs(avatarRef) {
                start.linkTo(startGuideline)
                top.linkTo(parent.top, margin = 16.dp)
            }
        )

        NostrUserText(
            displayName = "miljan",
            verifiedBadge = true,
            internetIdentifier = "miljan@primal.net",
            modifier = Modifier.constrainAs(usernameRef) {
                start.linkTo(startGuideline)
                top.linkTo(avatarRef.bottom, margin = 16.dp)
                width = Dimension.preferredValue(220.dp)
            }
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
            text = "miljan@primal.net",
            modifier = Modifier.constrainAs(identifierRef) {
                start.linkTo(startGuideline)
                top.linkTo(usernameRef.bottom, margin = 8.dp)
            }
        )


        Text(
            text = "135 Following  345 Followers",
            modifier = Modifier.constrainAs(statsRef) {
                start.linkTo(startGuideline)
                top.linkTo(identifierRef.bottom, margin = 8.dp)
            }
        )
    }
}


@Composable
private fun DrawerMenu(
    modifier: Modifier,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        items(
            items = DrawerScreenDestination.values(),
            key = { it.name }
        ) {
            ListItem(
                modifier = Modifier.clickable {
                    onDrawerDestinationClick(it)
                },
                headlineContent = {
                    Text(
                        text = it.label().uppercase(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        style = PrimalTheme.typography.bodyLarge,
                    )
                }
            )
        }
    }
}

@Composable
private fun DrawerFooter(
    onThemeSwitch: () -> Unit,
) {
    Box(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
    ) {
        IconButton(
            onClick = onThemeSwitch,
        ) {
            Icon(imageVector = PrimalIcons.DarkMode, contentDescription = null)
        }
    }

}

enum class DrawerScreenDestination {
    Profile, Bookmarks, UserLists, Settings, SignOut
}


@Composable
private fun DrawerScreenDestination.label(): String {
    return when (this) {
        DrawerScreenDestination.Profile -> stringResource(id = R.string.drawer_destination_profile)
        DrawerScreenDestination.Bookmarks -> stringResource(id = R.string.drawer_destination_bookmarks)
        DrawerScreenDestination.UserLists -> stringResource(id = R.string.drawer_destination_user_lists)
        DrawerScreenDestination.Settings -> stringResource(id = R.string.drawer_destination_settings)
        DrawerScreenDestination.SignOut -> stringResource(id = R.string.drawer_destination_sign_out)
    }
}

@Preview
@Composable
fun PrimalDrawerPreview() {
    PrimalTheme {
        PrimalDrawer(
            state = PrimalDrawerContract.UiState(),
            onDrawerDestinationClick = {},
        )
    }
}