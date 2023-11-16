package net.primal.android.settings.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun SettingsHomeScreen(
    viewModel: SettingsHomeViewModel,
    onClose: () -> Unit,
    onSettingsSectionClick: (PrimalSettingsSection) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    SettingsHomeScreen(
        state = uiState.value,
        onClose = onClose,
        onSettingsSectionClick = onSettingsSectionClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    state: SettingsHomeContract.UiState,
    onClose: () -> Unit,
    onSettingsSectionClick: (PrimalSettingsSection) -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
            ) {
                items(
                    items = PrimalSettingsSection.values(),
                    key = { it.name },
                ) {
                    SettingsListItem(
                        title = it.title(),
                        onClick = { onSettingsSectionClick(it) },
                        leadingIcon = it.leadingIcon(),
                        trailingIcon = Icons.Outlined.KeyboardArrowRight,
                    )
                    PrimalDivider()
                }

                item {
                    VersionListItem(
                        versionName = state.version,
                    )
                }
            }
        },
    )
}

@Composable
private fun SettingsListItem(
    title: String,
    onClick: () -> Unit,
    description: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            if (leadingIcon != null) {
                Icon(imageVector = leadingIcon, contentDescription = null)
            }
        },
        headlineContent = {
            Text(
                text = title,
                style = AppTheme.typography.titleLarge,
            )
        },
        supportingContent = {
            if (description != null) {
                Text(
                    text = description,
                )
            }
        },
        trailingContent = {
            if (trailingIcon != null) {
                Icon(imageVector = trailingIcon, contentDescription = null)
            }
        },
    )
}

@Composable
private fun VersionListItem(versionName: String) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = R.string.settings_version_title).uppercase(),
                style = AppTheme.typography.bodySmall,
            )
        },
        supportingContent = {
            Text(
                text = versionName,
                style = AppTheme.typography.titleLarge,
            )
        },
        leadingContent = { },
    )
}

private fun PrimalSettingsSection.leadingIcon(): ImageVector? {
    return when (this) {
        PrimalSettingsSection.Account -> null
        PrimalSettingsSection.Wallet -> null
        PrimalSettingsSection.Appearance -> null
        PrimalSettingsSection.Notifications -> null
        PrimalSettingsSection.Feeds -> null
        PrimalSettingsSection.Zaps -> null
        PrimalSettingsSection.MutedAccounts -> null
    }
}

@Composable
private fun PrimalSettingsSection.title(): String {
    return when (this) {
        PrimalSettingsSection.Account -> stringResource(id = R.string.settings_keys_title)
        PrimalSettingsSection.Wallet -> stringResource(id = R.string.settings_wallet_title)
        PrimalSettingsSection.Appearance -> stringResource(id = R.string.settings_appearance_title)
        PrimalSettingsSection.Notifications -> stringResource(
            id = R.string.settings_notifications_title,
        )
        PrimalSettingsSection.Feeds -> stringResource(id = R.string.settings_feeds_title)
        PrimalSettingsSection.Zaps -> stringResource(id = R.string.settings_zaps_title)
        PrimalSettingsSection.MutedAccounts -> stringResource(
            id = R.string.settings_muted_accounts_title,
        )
    }
}

@Preview
@Composable
fun PreviewSettingsHomeScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        SettingsHomeScreen(
            state = SettingsHomeContract.UiState(version = "1.1"),
            onClose = { },
            onSettingsSectionClick = {},
        )
    }
}
