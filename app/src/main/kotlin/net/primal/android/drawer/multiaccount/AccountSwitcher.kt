package net.primal.android.drawer.multiaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.core.compose.DefaultAvatarThumbnailPlaceholderListItemImage
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.drawer.multiaccount.ui.AccountSwitcherBottomSheet
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun AccountSwitcher(
    modifier: Modifier = Modifier,
    callbacks: AccountSwitcherCallbacks,
    onLogoutClick: (String) -> Unit,
) {
    val viewModel = hiltViewModel<AccountSwitcherViewModel>()
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                AccountSwitcherContract.SideEffect.AccountSwitched -> callbacks.onActiveAccountChanged()
            }
        }
    }

    AccountSwitcher(
        modifier = modifier,
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        callbacks = callbacks,
        onLogoutClick = onLogoutClick,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun AccountSwitcher(
    modifier: Modifier = Modifier,
    eventPublisher: (AccountSwitcherContract.UiEvent) -> Unit,
    state: AccountSwitcherContract.UiState,
    callbacks: AccountSwitcherCallbacks,
    onLogoutClick: (String) -> Unit,
) {
    var accountsBottomSheetVisibility by remember { mutableStateOf(false) }
    val bottomSheetIcon = if (state.userAccounts.isEmpty()) {
        Icons.Default.Add
    } else {
        PrimalIcons.More
    }

    if (accountsBottomSheetVisibility && state.activeAccount != null) {
        AccountSwitcherBottomSheet(
            activeAccount = state.activeAccount,
            accounts = state.userAccounts,
            onDismissRequest = { accountsBottomSheetVisibility = false },
            onLogoutClick = onLogoutClick,
            onCreateNewAccountClick = callbacks.onCreateNewAccountClick,
            onAddExistingAccountClick = callbacks.onAddExistingAccountClick,
            onAccountClick = { eventPublisher(AccountSwitcherContract.UiEvent.SwitchAccount(it)) },
        )
    }

    Column(
        modifier = modifier
            .padding(end = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.userAccounts.take(2).forEach { account ->
            UniversalAvatarThumbnail(
                modifier = Modifier.clip(CircleShape),
                avatarSize = 28.dp,
                avatarCdnImage = account.avatarCdnImage,
                avatarBlossoms = account.avatarBlossoms,
                legendaryCustomization = account.legendaryCustomization,
                onClick = { eventPublisher(AccountSwitcherContract.UiEvent.SwitchAccount(account.pubkey)) },
                defaultAvatar = {
                    CompositionLocalProvider(
                        LocalContentColor provides AppTheme.colorScheme.onSurface,
                    ) {
                        DefaultAvatarThumbnailPlaceholderListItemImage()
                    }
                },
            )
        }

        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            IconButton(onClick = { accountsBottomSheetVisibility = true }) {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1, shape = CircleShape)
                        .padding(8.dp),
                    imageVector = bottomSheetIcon,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
    }
}
