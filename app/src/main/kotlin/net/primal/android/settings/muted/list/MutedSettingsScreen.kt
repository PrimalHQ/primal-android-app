package net.primal.android.settings.muted.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.muted.list.model.MutedUserUi
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun MutedSettingsScreen(
    viewModel: MutedSettingsViewModel,
    onProfileClick: (String) -> Unit,
    onClose: () -> Unit,
) {
    val state = viewModel.state.collectAsState()
    MutedSettingsScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onProfileClick = onProfileClick,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutedSettingsScreen(
    state: MutedSettingsContract.UiState,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_muted_accounts_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding(),
            ) {
                items(
                    items = state.mutedUsers,
                    key = { it.userId },
                    contentType = { "MutedUser" },
                ) { mutedUser ->
                    MutedUserListItem(
                        item = mutedUser,
                        onUnmuteClick = {
                            eventPublisher(
                                MutedSettingsContract.UiEvent.UnmuteEvent(mutedUser.userId),
                            )
                        },
                        onProfileClick = onProfileClick,
                    )
                    PrimalDivider()
                }

                if (state.mutedUsers.isEmpty()) {
                    item(contentType = "NoContent") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(
                                id = R.string.settings_muted_accounts_no_content,
                            ),
                            refreshButtonVisible = false,
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun MutedUserListItem(
    item: MutedUserUi,
    onUnmuteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            AvatarThumbnail(
                avatarCdnImage = item.avatarCdnImage,
                onClick = { onProfileClick(item.userId) },
            )
        },
        headlineContent = {
            NostrUserText(
                displayName = item.displayName,
                fontSize = 14.sp,
                internetIdentifier = item.internetIdentifier,
            )
        },
        supportingContent = {
            if (!item.internetIdentifier.isNullOrEmpty()) {
                Text(
                    text = item.internetIdentifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    fontSize = 14.sp,
                )
            }
        },
        trailingContent = {
            PrimalFilledButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(36.dp),
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onSurface,
                textStyle = AppTheme.typography.titleMedium.copy(
                    lineHeight = 18.sp,
                ),
                onClick = { onUnmuteClick(item.userId) },
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_muted_accounts_unmute_button,
                    ).lowercase(),
                )
            }
        },
    )
}

@Preview
@Composable
fun PreviewMutedScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        MutedSettingsScreen(
            state = MutedSettingsContract.UiState(
                mutedUsers = listOf(
                    MutedUserUi(
                        userId = "pubkey",
                        displayName = "username",
                        avatarCdnImage = null,
                        internetIdentifier = "nip05",
                    ),
                    MutedUserUi(
                        userId = "pubkey",
                        displayName = "username",
                        avatarCdnImage = CdnImage("avatarUrl"),
                        internetIdentifier = "nip05",
                    ),
                    MutedUserUi(
                        userId = "pubkey",
                        displayName = "username",
                        avatarCdnImage = null,
                        internetIdentifier = "nip05",
                    ),
                    MutedUserUi(
                        userId = "pubkey",
                        displayName = "username",
                        avatarCdnImage = null,
                        internetIdentifier = "nip05",
                    ),
                    MutedUserUi(
                        userId = "pubkey",
                        displayName = "username",
                        avatarCdnImage = CdnImage("avatarUrl"),
                        internetIdentifier = "nip05",
                    ),
                ),
            ),
            eventPublisher = {},
            onProfileClick = {},
            onClose = {},
        )
    }
}
