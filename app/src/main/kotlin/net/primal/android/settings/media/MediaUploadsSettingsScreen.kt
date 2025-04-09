package net.primal.android.settings.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ConnectRelay
import net.primal.android.core.compose.settings.DecoratedSettingsOutlinedTextField
import net.primal.android.settings.network.TextSection
import net.primal.android.theme.AppTheme

@Composable
fun MediaUploadsSettingsScreen(viewModel: MediaUploadsSettingsViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()
    MediaUploadsSettingsScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaUploadsSettingsScreen(
    state: MediaUploadsSettingsContract.UiState,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_media_uploads_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            MediaUploadsLazyColumn(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(paddingValues)
                    .imePadding(),
                state = state,
                eventPublisher = eventPublisher,
            )
        },
    )
}

@Composable
private fun MediaUploadsLazyColumn(
    modifier: Modifier,
    state: MediaUploadsSettingsContract.UiState,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val isEditMainBlossom = state.mode == MediaUploadsMode.EditBlossomServer
    val isEditMirrorBlossom = state.mode == MediaUploadsMode.EditBlossomMirrorServer
    val isViewMode = state.mode == MediaUploadsMode.View

    LazyColumn(modifier = modifier) {
        if (isViewMode) {
            blossomMainServerSectionItem(state.blossomServerUrl)
        }

        if (isEditMainBlossom || isViewMode) {
            blossomMainServerInputItem(state, eventPublisher, keyboardController)
            item { PrimalDivider() }
        }

        if (isViewMode) {
            blossomMirrorServerSectionItem(
                blossomMirrorEnabled = state.blossomMirrorEnabled,
                onBlossomMirrorCheckedChange = {
                    eventPublisher(
                        MediaUploadsSettingsContract.UiEvent.UpdateBlossomMirrorEnabled(it),
                    )
                },
            )
        }

        if (state.blossomMirrorEnabled) {
            if (isViewMode) {
                item {
                    BlossomServerDestination(
                        modifier = Modifier.padding(vertical = 8.dp),
                        destinationUrl = state.blossomServerMirrorUrl,
                    )
                }
            }

            if (isEditMirrorBlossom || isViewMode) {
                blossomMirrorServerInputItem(state, eventPublisher, keyboardController)
            }

            if (isEditMirrorBlossom) {
                item { PrimalDivider() }
            }
        }

        if (isEditMainBlossom || isEditMirrorBlossom) {
            suggestedBlossomServersSectionItems(
                suggestedBlossomServerUrls = state.suggestedBlossomServers,
                confirmBlossomServerUrl = { url ->
                    val event = if (isEditMainBlossom) {
                        MediaUploadsSettingsContract.UiEvent.ConfirmBlossomServerUrl(url)
                    } else {
                        MediaUploadsSettingsContract.UiEvent.ConfirmBlossomMirrorServerUrl(url)
                    }
                    eventPublisher(event)
                },
            )
        }
    }
}

private fun LazyListScope.suggestedBlossomServersSectionItems(
    suggestedBlossomServerUrls: List<String>,
    confirmBlossomServerUrl: (String) -> Unit,
) {
    item {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.settings_media_uploads_suggested_blossoms_title),
                style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    items(suggestedBlossomServerUrls) { server ->
        SuggestedServerItem(
            server = server,
            onClick = {
                confirmBlossomServerUrl(server)
            },
        )
    }
}

@Composable
private fun SuggestedServerItem(server: String, onClick: () -> Unit) {
    val success = AppTheme.extraColorScheme.successBright

    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp, top = 2.dp)
                    .size(10.dp)
                    .drawWithCache {
                        onDrawWithContent {
                            drawCircle(color = success)
                        }
                    },
            )
        },
        headlineContent = {
            Text(text = server, color = AppTheme.extraColorScheme.onSurfaceVariantAlt1)
        },
        trailingContent = {
            AppBarIcon(
                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
                icon = PrimalIcons.ConnectRelay,
                tint = AppTheme.colorScheme.primary,
                iconSize = 19.dp,
                onClick = onClick,
                appBarIconContentDescription = "",
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}

private fun LazyListScope.blossomMainServerSectionItem(blossomServerUrl: String) {
    item {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            TextSection(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.settings_media_uploads_blossom_server_section_title).uppercase(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            BlossomServerDestination(destinationUrl = blossomServerUrl)
        }
    }
}

private fun LazyListScope.blossomMainServerInputItem(
    state: MediaUploadsSettingsContract.UiState,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
    keyboardController: SoftwareKeyboardController?,
) {
    item(key = "blossom_main_input") {
        DecoratedSettingsOutlinedTextField(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            title = stringResource(id = R.string.settings_media_uploads_switch_blossom_server),
            placeholderText = stringResource(id = R.string.settings_media_uploads_switch_blossom_server_placeholder),
            value = state.newBlossomServerUrl,
            onValueChanged = {
                eventPublisher(MediaUploadsSettingsContract.UiEvent.UpdateNewBlossomServerUrl(url = it))
            },
            supportingActionText = stringResource(R.string.settings_media_uploads_restore_default_blossom_server),
            onSupportActionClick = {
                eventPublisher(MediaUploadsSettingsContract.UiEvent.RestoreDefaultBlossomServer)
            },
            showSupportContent = state.mode == MediaUploadsMode.View,
            buttonEnabled = state.newBlossomServerUrl != state.blossomServerUrl &&
                state.newBlossomServerUrl != state.blossomServerMirrorUrl &&
                state.newBlossomServerUrl.isValidBlossomUrl(),
            onActionClick = {
                keyboardController?.hide()
                eventPublisher(
                    MediaUploadsSettingsContract.UiEvent.ConfirmBlossomServerUrl(url = state.newBlossomServerUrl),
                )
            },
        )
    }

    if (state.mode == MediaUploadsMode.EditBlossomServer) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun LazyListScope.blossomMirrorServerInputItem(
    state: MediaUploadsSettingsContract.UiState,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
    keyboardController: SoftwareKeyboardController?,
) {
    item(key = "blossom_mirror_input") {
        DecoratedSettingsOutlinedTextField(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            title = stringResource(id = R.string.settings_media_uploads_switch_blossom_mirror_server),
            placeholderText = stringResource(id = R.string.settings_media_uploads_switch_blossom_server_placeholder),
            value = state.newBlossomServerMirrorUrl,
            onValueChanged = {
                eventPublisher(MediaUploadsSettingsContract.UiEvent.UpdateNewBlossomMirrorServerUrl(url = it))
            },
            supportingActionText = "",
            onSupportActionClick = {},
            showSupportContent = false,
            buttonEnabled = state.newBlossomServerMirrorUrl != state.blossomServerUrl &&
                state.newBlossomServerMirrorUrl != state.blossomServerMirrorUrl &&
                state.newBlossomServerMirrorUrl.isValidBlossomUrl(),
            onActionClick = {
                keyboardController?.hide()
                eventPublisher(
                    MediaUploadsSettingsContract.UiEvent.ConfirmBlossomMirrorServerUrl(
                        url = state.newBlossomServerMirrorUrl,
                    ),
                )
            },
        )
    }

    if (state.mode == MediaUploadsMode.EditBlossomMirrorServer) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun LazyListScope.blossomMirrorServerSectionItem(
    blossomMirrorEnabled: Boolean,
    onBlossomMirrorCheckedChange: (Boolean) -> Unit,
) {
    item {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            TextSection(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(
                    id = R.string.settings_media_uploads_blossom_mirror_server_section_title,
                ).uppercase(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(AppTheme.shapes.medium),
                headlineContent = {
                    Text(
                        text = stringResource(
                            id = R.string.settings_media_uploads_blossom_mirror_server_switcher_title,
                        ),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colorScheme.onPrimary,
                    )
                },
                trailingContent = {
                    PrimalSwitch(
                        checked = blossomMirrorEnabled,
                        onCheckedChange = onBlossomMirrorCheckedChange,
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(id = R.string.settings_media_uploads_blossom_mirror_server_enabled_notice),
                style = AppTheme.typography.bodySmall,
                lineHeight = 20.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}

@Composable
private fun BlossomServerDestination(
    modifier: Modifier = Modifier,
    destinationUrl: String,
    connected: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    ),
) {
    val success = AppTheme.extraColorScheme.successBright
    val failed = AppTheme.colorScheme.error

    ListItem(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(AppTheme.shapes.medium),
        leadingContent = {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp, top = 2.dp)
                    .size(10.dp)
                    .drawWithCache {
                        this.onDrawWithContent {
                            drawCircle(color = if (connected) success else failed)
                        }
                    },
            )
        },
        headlineContent = {
            Text(text = destinationUrl)
        },
        colors = colors,
    )
}

private fun String.isValidBlossomUrl() =
    (startsWith("http://") || startsWith("https://")) &&
        !this.split("://").lastOrNull().isNullOrEmpty()
