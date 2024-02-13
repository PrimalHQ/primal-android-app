package net.primal.android.settings.network

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.DeleteListItemImage
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ConnectRelay
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
    var confirmingRestoreDefaultRelaysDialog by remember { mutableStateOf(false) }
    var confirmingRelayDeletionDialog by remember { mutableStateOf<String?>(null) }

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

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_network_title),
                navigationIcon = PrimalIcons.ArrowBack,
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
                onRemoveClick = { confirmingRelayDeletionDialog = it },
                onRestoreDefaultsClick = { confirmingRestoreDefaultRelaysDialog = true },
            )
        },
    )
}

@Composable
private fun NetworkLazyColumn(
    modifier: Modifier,
    eventsPublisher: (NetworkSettingsContract.UiEvent) -> Unit,
    state: NetworkSettingsContract.UiState,
    onRemoveClick: (String) -> Unit,
    onRestoreDefaultsClick: () -> Unit,
) {
    var newRelayText by remember { mutableStateOf("") }

    LazyColumn(modifier = modifier) {
        item {
            TextSection(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                text = stringResource(id = R.string.settings_network_relays_section).uppercase(),
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                TextSubSection(
                    text = stringResource(id = R.string.settings_network_connect_to_relay_section).uppercase(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                NewRelayOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = newRelayText,
                    onValueChange = { newRelayText = it },
                    onAddRelayConfirmed = {
                        eventsPublisher(NetworkSettingsContract.UiEvent.AddRelay(url = newRelayText))
                    },
                )
            }
        }

        item {
            TextSubSection(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                text = stringResource(id = R.string.settings_network_my_relays_section).uppercase(),
            )
        }

        items(items = state.relays, key = { it.url }) {
            Column {
                NetworkDestinationListItem(
                    destinationUrl = it.url,
                    connected = true,
                    onRemoveClick = { onRemoveClick(it.url) },
                )
                PrimalDivider()
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    modifier = Modifier.clickable { onRestoreDefaultsClick() },
                    text = stringResource(R.string.settings_network_restore_default_relays_text_button).lowercase(),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.secondary,
                )
            }
        }

        item {
            TextSection(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                text = stringResource(id = R.string.settings_network_caching_service_section).uppercase(),
            )
        }

        item {
            if (state.cachingUrl.isNotEmpty()) {
                NetworkDestinationListItem(
                    destinationUrl = state.cachingUrl,
                    connected = true,
                )
            }
        }
    }
}

@Composable
private fun NewRelayOutlinedTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onAddRelayConfirmed: () -> Unit,
) {
    OutlinedTextField(
        modifier = modifier,
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
        trailingIcon = {
            Image(
                imageVector = PrimalIcons.ConnectRelay,
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                ),
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(
            onGo = { onAddRelayConfirmed() },
        ),
    )
}

@Composable
fun TextSection(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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
                    text = stringResource(id = R.string.settings_feeds_dialog_confirm),
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
                    text = stringResource(id = R.string.settings_feeds_dialog_dismiss),
                )
            }
        },
    )
}
