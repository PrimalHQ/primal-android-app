package net.primal.android.settings.network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.DeleteListItemImage
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ConnectRelay
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.theme.AppTheme

@Composable
fun NetworkSettingsScreen(viewModel: NetworkSettingsViewModel, onClose: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    NetworkSettingsScreen(
        state = uiState,
        onClose = onClose,
        eventsPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen(
    state: NetworkSettingsContract.UiState,
    onClose: () -> Unit,
    eventsPublisher: (NetworkSettingsContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is NetworkSettingsContract.UiState.NetworkSettingsError.FailedToAddRelay ->
                    context.getString(R.string.settings_network_add_relay_error)
            }
        },
        onErrorDismiss = { eventsPublisher(NetworkSettingsContract.UiEvent.DismissError) },
    )

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_network_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            NetworkLazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .imePadding(),
                state = state,
                eventsPublisher = eventsPublisher,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun NetworkLazyColumn(
    modifier: Modifier,
    state: NetworkSettingsContract.UiState,
    eventsPublisher: (NetworkSettingsContract.UiEvent) -> Unit,
) {
    var confirmingRestoreCachingServiceDialog by remember { mutableStateOf(false) }
    if (confirmingRestoreCachingServiceDialog) {
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_network_restore_default_caching_service_title),
            dialogText = stringResource(id = R.string.settings_network_restore_default_caching_service_description),
            onDismissRequest = {
                confirmingRestoreCachingServiceDialog = false
            },
            onConfirmation = {
                confirmingRestoreCachingServiceDialog = false
                eventsPublisher(NetworkSettingsContract.UiEvent.RestoreDefaultCachingService)
            },
        )
    }

    var confirmingRestoreDefaultRelaysDialog by remember { mutableStateOf(false) }
    if (confirmingRestoreDefaultRelaysDialog) {
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_network_restore_default_relays_title),
            dialogText = stringResource(id = R.string.settings_network_restore_default_relays_description),
            onDismissRequest = {
                confirmingRestoreDefaultRelaysDialog = false
            },
            onConfirmation = {
                confirmingRestoreDefaultRelaysDialog = false
                eventsPublisher(NetworkSettingsContract.UiEvent.RestoreDefaultRelays)
            },
        )
    }

    var confirmingRelayDeletionDialog by remember { mutableStateOf<String?>(null) }
    confirmingRelayDeletionDialog?.let { relayUrl ->
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_network_delete_relay_title),
            dialogText = stringResource(
                id = R.string.settings_network_delete_relay_description,
                relayUrl,
            ),
            onDismissRequest = {
                confirmingRelayDeletionDialog = null
            },
            onConfirmation = {
                confirmingRelayDeletionDialog = null
                eventsPublisher(NetworkSettingsContract.UiEvent.DeleteRelay(url = relayUrl))
            },
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    LazyColumn(modifier = modifier) {
        enhancedPrivacyItem(
            checked = state.cachingProxyEnabled,
            onCheckedChanged = { enabled ->
                eventsPublisher(NetworkSettingsContract.UiEvent.UpdateCachingProxyFlag(enabled = enabled))
            },
        )

        item { PrimalDivider() }

        if (state.cachingService != null) {
            cachingServiceSectionItems(
                state = state,
                onRestoreDefaultCachingService = { confirmingRestoreCachingServiceDialog = true },
                keyboardController = keyboardController,
                eventsPublisher = eventsPublisher,
            )
        }

        item { PrimalDivider() }

        relaysSectionItems(
            state = state,
            onRemoveRelayClick = { url -> confirmingRelayDeletionDialog = url },
            onRestoreDefaultRelaysClick = { confirmingRestoreDefaultRelaysDialog = true },
            keyboardController = keyboardController,
            eventsPublisher = eventsPublisher,
        )

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

private fun LazyListScope.enhancedPrivacyItem(checked: Boolean, onCheckedChanged: (Boolean) -> Unit) {
    item {
        Column {
            SettingsItem(
                modifier = Modifier.padding(vertical = 8.dp),
                headlineText = stringResource(id = R.string.settings_network_enhanced_privacy_title),
                supportText = stringResource(id = R.string.settings_network_enhanced_privacy_description),
                trailingContent = {
                    PrimalSwitch(checked = checked, onCheckedChange = onCheckedChanged)
                },
            )
        }
    }
}

private fun LazyListScope.relaysSectionItems(
    state: NetworkSettingsContract.UiState,
    onRemoveRelayClick: (String) -> Unit,
    onRestoreDefaultRelaysClick: () -> Unit,
    keyboardController: SoftwareKeyboardController?,
    eventsPublisher: (NetworkSettingsContract.UiEvent) -> Unit,
) {
    item {
        Column {
            Spacer(modifier = Modifier.height(8.dp))
            TextSection(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                text = stringResource(id = R.string.settings_network_relays_section).uppercase(),
            )
            PrimalDivider()
        }
    }

    items(items = state.relays, key = { it.url }) {
        Column {
            NetworkDestinationListItem(
                destinationUrl = it.url,
                connected = it.connected,
                onRemoveClick = { onRemoveRelayClick(it.url) },
            )
            PrimalDivider()
        }
    }

    item {
        DecoratedRelayOutlinedTextField(
            modifier = Modifier.padding(horizontal = 16.dp),
            performingAction = state.updatingRelays,
            relayValue = state.newRelayUrl,
            onRelayValueChanged = {
                eventsPublisher(NetworkSettingsContract.UiEvent.UpdateNewRelayUrl(it))
            },
            title = stringResource(id = R.string.settings_network_add_a_relay_section),
            supportingActionText = stringResource(R.string.settings_network_restore_default_relays_text_button),
            onSupportActionClick = onRestoreDefaultRelaysClick,
            onActionClick = {
                keyboardController?.hide()
                eventsPublisher(NetworkSettingsContract.UiEvent.ConfirmRelayInsert(url = state.newRelayUrl))
            },
        )
    }
}

private fun LazyListScope.cachingServiceSectionItems(
    state: NetworkSettingsContract.UiState,
    onRestoreDefaultCachingService: () -> Unit,
    keyboardController: SoftwareKeyboardController?,
    eventsPublisher: (NetworkSettingsContract.UiEvent) -> Unit,
) {
    if (state.cachingService == null) return

    item {
        CachingServiceSection(
            url = state.cachingService.url,
            connected = state.cachingService.connected,
        )
    }

    item {
        DecoratedRelayOutlinedTextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            performingAction = state.updatingCachingService,
            relayValue = state.newCachingServiceUrl,
            onRelayValueChanged = {
                eventsPublisher(NetworkSettingsContract.UiEvent.UpdateNewCachingServiceUrl(it))
            },
            title = stringResource(id = R.string.settings_network_switch_caching_service),
            supportingActionText = stringResource(
                R.string.settings_network_restore_default_caching_service_text_button,
            ),
            onSupportActionClick = onRestoreDefaultCachingService,
            onActionClick = {
                keyboardController?.hide()
                eventsPublisher(
                    NetworkSettingsContract.UiEvent.ConfirmCachingServiceChange(url = state.newCachingServiceUrl),
                )
            },
        )
    }
}

@Composable
private fun CachingServiceSection(url: String, connected: Boolean) {
    Column {
        Spacer(modifier = Modifier.height(24.dp))
        TextSection(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.settings_network_caching_service_section).uppercase(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        NetworkDestinationListItem(destinationUrl = url, connected = connected)
    }
}

@Composable
private fun DecoratedRelayOutlinedTextField(
    modifier: Modifier,
    relayValue: String,
    onRelayValueChanged: (String) -> Unit,
    performingAction: Boolean,
    title: String,
    supportingActionText: String,
    onActionClick: () -> Unit,
    onSupportActionClick: () -> Unit,
) {
    Column(modifier = modifier) {
        TextSubSection(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            text = title.uppercase(),
        )
        RelayOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = relayValue,
            buttonEnabled = !performingAction && relayValue.isValidRelayUrl(),
            buttonContentDescription = stringResource(id = R.string.accessibility_connect_relay),
            onValueChange = onRelayValueChanged,
            onButtonClick = onActionClick,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                modifier = Modifier.clickable { onSupportActionClick() },
                text = supportingActionText.lowercase(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun RelayOutlinedTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    buttonEnabled: Boolean,
    buttonContentDescription: String,
    onButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1.0f),
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.medium,
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AppTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = "wss://",
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyMedium,
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = { onButtonClick() },
            ),
        )

        AppBarIcon(
            modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
            icon = PrimalIcons.ConnectRelay,
            enabledBackgroundColor = AppTheme.colorScheme.primary,
            tint = Color.White,
            enabled = buttonEnabled,
            onClick = onButtonClick,
            appBarIconContentDescription = buttonContentDescription,
        )
    }
}

@Composable
fun TextSection(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun TextSubSection(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
    )
}

@Composable
fun NetworkDestinationListItem(
    destinationUrl: String,
    connected: Boolean,
    onRemoveClick: (() -> Unit)? = null,
) {
    val success = AppTheme.extraColorScheme.successBright
    val failed = AppTheme.colorScheme.error
    ListItem(
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
        trailingContent = {
            if (onRemoveClick != null) {
                DeleteListItemImage(
                    modifier = Modifier.clickable { onRemoveClick() },
                )
            }
        },
    )
}

@Composable
fun ConfirmActionAlertDialog(
    dialogTitle: String,
    dialogText: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.settings_network_dialog_confirm),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.settings_network_dialog_dismiss),
                )
            }
        },
    )
}

private fun String.isValidRelayUrl() =
    (startsWith("wss://") || startsWith("ws://")) &&
        !this.split("://").lastOrNull().isNullOrEmpty()

@Composable
@Preview
private fun PreviewNetworksScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        NetworkSettingsScreen(
            state = NetworkSettingsContract.UiState(
                relays = listOf(
                    SocketDestinationUiState(
                        url = "wss://primal.relay.net",
                        connected = false,
                    ),
                ),
                cachingService = SocketDestinationUiState(
                    url = "wss://cache.primal.net/v1",
                    connected = true,
                ),
            ),
            onClose = {},
            eventsPublisher = {},
        )
    }
}
