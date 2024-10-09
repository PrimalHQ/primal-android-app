package net.primal.android.settings.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
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
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
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
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
            ) {
                items(
                    items = PrimalSettingsSection.entries,
                    key = { it.name },
                ) {
                    SettingsListItem(
                        title = it.title(),
                        onClick = { onSettingsSectionClick(it) },
                        trailingIcon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
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
        modifier = Modifier
            .height(60.dp)
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = if (leadingIcon != null) {
            {
                Icon(imageVector = leadingIcon, contentDescription = null)
            }
        } else {
            null
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
                modifier = Modifier.padding(top = 8.dp),
                text = versionName,
                style = AppTheme.typography.titleLarge,
            )
        },
    )
}

@Composable
private fun PrimalSettingsSection.title(): String {
    return when (this) {
        PrimalSettingsSection.Account -> stringResource(id = R.string.settings_keys_title)
        PrimalSettingsSection.Network -> stringResource(id = R.string.settings_network_title)
        PrimalSettingsSection.Wallet -> stringResource(id = R.string.settings_wallet_title)
        PrimalSettingsSection.Appearance -> stringResource(id = R.string.settings_appearance_title)
        PrimalSettingsSection.ContentDisplay -> stringResource(id = R.string.settings_content_display_title)
        PrimalSettingsSection.Notifications -> stringResource(id = R.string.settings_notifications_title)
        PrimalSettingsSection.Zaps -> stringResource(id = R.string.settings_zaps_title)
        PrimalSettingsSection.MutedAccounts -> stringResource(id = R.string.settings_muted_accounts_title)
    }
}

@Preview
@Composable
fun PreviewSettingsHomeScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunrise) {
        SettingsHomeScreen(
            state = SettingsHomeContract.UiState(version = "1.1"),
            onClose = { },
            onSettingsSectionClick = {},
        )
    }
}
