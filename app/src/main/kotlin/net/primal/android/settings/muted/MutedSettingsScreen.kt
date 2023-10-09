package net.primal.android.settings.muted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.muted.model.MutedUser
import net.primal.android.theme.PrimalTheme

@Composable
fun MutedSettingsScreen(
    viewModel: MutedSettingsViewModel,
    onClose: () -> Unit
) {
    val state = viewModel.state.collectAsState()

    MutedSettingsScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutedSettingsScreen(
    state: MutedSettingsContract.UiState,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = "Muted Accounts",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                items(state.mutelist) { item ->
                    MutedUserItem(item = item, eventPublisher = eventPublisher)
                }
            }
        }
    )
}

@Composable
fun MutedUserItem(
    item: MutedUser,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit
) {

}

@Preview
@Composable
fun PreviewMutedScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        MutedSettingsScreen(
            state = MutedSettingsContract.UiState(
                mutelist = listOf(
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = "avatarUrl",
                        nip05 = "nip05"
                    ),
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = "avatarUrl",
                        nip05 = "nip05"
                    ),
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = "avatarUrl",
                        nip05 = "nip05"
                    ),
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = "avatarUrl",
                        nip05 = "nip05"
                    ), MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = "avatarUrl",
                        nip05 = "nip05"
                    )
                )
            ),
            eventPublisher = {},
            onClose = {})
    }
}