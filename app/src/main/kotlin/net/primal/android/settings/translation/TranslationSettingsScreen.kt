package net.primal.android.settings.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.settings.translation.TranslationSettingsContract.UiState
import net.primal.android.theme.AppTheme

@Composable
fun TranslationSettingsScreen(
    viewModel: TranslationSettingsViewModel,
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    TranslationSettingsScreen(
        state = state,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsScreen(
    state: UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(id = R.string.settings_translation_saved)

    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar(savedMessage)
            eventPublisher(UiEvent.SavedMessageShown)
        }
    }

    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_translation_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            TranslationSettingsContent(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .imePadding(),
                state = state,
                eventPublisher = eventPublisher,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun TranslationSettingsContent(
    modifier: Modifier,
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        item {
            SettingsItem(
                modifier = Modifier.padding(vertical = 8.dp),
                headlineText = stringResource(id = R.string.settings_translation_enabled_title),
                supportText = stringResource(id = R.string.settings_translation_enabled_description),
                trailingContent = {
                    PrimalSwitch(
                        checked = state.enabled,
                        onCheckedChange = { eventPublisher(UiEvent.EnabledChanged(it)) },
                    )
                },
                onClick = { eventPublisher(UiEvent.EnabledChanged(!state.enabled)) },
            )
        }

        item {
            TranslationTextField(
                value = state.endpoint,
                onValueChange = { eventPublisher(UiEvent.EndpointChanged(it)) },
                title = stringResource(id = R.string.settings_translation_endpoint_title),
                placeholder = stringResource(id = R.string.settings_translation_endpoint_hint),
                keyboardType = KeyboardType.Uri,
            )
        }

        item {
            TranslationTextField(
                value = state.apiKey,
                onValueChange = { eventPublisher(UiEvent.ApiKeyChanged(it)) },
                title = stringResource(id = R.string.settings_translation_api_key_title),
                placeholder = stringResource(id = R.string.settings_translation_api_key_hint),
                keyboardType = KeyboardType.Text,
            )
        }

        item {
            TranslationTextField(
                value = state.targetLanguage,
                onValueChange = { eventPublisher(UiEvent.TargetLanguageChanged(it)) },
                title = stringResource(id = R.string.settings_translation_target_language_title),
                placeholder = stringResource(id = R.string.settings_translation_target_language_hint),
                keyboardType = KeyboardType.Text,
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { eventPublisher(UiEvent.RestoreDefaults) }) {
                    Text(text = stringResource(id = R.string.settings_translation_restore_defaults))
                }
                TextButton(onClick = { eventPublisher(UiEvent.Save) }) {
                    Text(text = stringResource(id = R.string.settings_translation_save))
                }
            }
        }
    }
}

@Composable
private fun TranslationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    placeholder: String,
    keyboardType: KeyboardType,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = title.uppercase(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.medium,
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AppTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = placeholder,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyMedium,
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = keyboardType,
                imeAction = ImeAction.Done,
            ),
        )
    }
}
