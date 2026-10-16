package net.primal.android.settings.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.notes.translation.NoteTranslationLanguages
import net.primal.android.settings.network.TextSubSection
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.NoteTranslationProvider
import net.primal.android.user.domain.NoteTranslationSettings

@Composable
fun TranslationSettingsScreen(viewModel: TranslationSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()
    TranslationSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslationSettingsScreen(
    state: TranslationSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_note_translation_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                EnableTranslationItem(state = state, eventPublisher = eventPublisher)
                Spacer(modifier = Modifier.height(8.dp))
                TargetLanguageItem(state = state, eventPublisher = eventPublisher)
                Spacer(modifier = Modifier.height(8.dp))
                ProviderItem(state = state, eventPublisher = eventPublisher)
                ProviderFields(state = state, eventPublisher = eventPublisher)
                StatusAndPolicy(state = state)
                Spacer(modifier = Modifier.height(32.dp))
            }
        },
    )
}

@Composable
private fun EnableTranslationItem(
    state: TranslationSettingsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    SettingsItem(
        headlineText = stringResource(id = R.string.settings_note_translation_enable),
        supportText = stringResource(id = R.string.settings_note_translation_enable_hint),
        trailingContent = {
            PrimalSwitch(
                checked = state.enabled,
                onCheckedChange = { eventPublisher(UiEvent.UpdateEnabled(enabled = it)) },
            )
        },
        onClick = { eventPublisher(UiEvent.UpdateEnabled(enabled = !state.enabled)) },
    )
}

@Composable
private fun TargetLanguageItem(
    state: TranslationSettingsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    var pickerVisible by remember { mutableStateOf(false) }
    SettingsItem(
        headlineText = stringResource(id = R.string.settings_note_translation_target_language),
        supportText = NoteTranslationLanguages.displayName(state.targetLanguageCode),
        onClick = { pickerVisible = true },
    )
    if (pickerVisible) {
        TranslationChoiceSheet(
            title = stringResource(id = R.string.settings_note_translation_target_language),
            options = languageOptions(),
            selectedKey = state.targetLanguageCode,
            onSelect = { eventPublisher(UiEvent.UpdateTargetLanguage(languageCode = it)) },
            onDismiss = { pickerVisible = false },
        )
    }
}

@Composable
private fun ProviderItem(
    state: TranslationSettingsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    var pickerVisible by remember { mutableStateOf(false) }
    SettingsItem(
        headlineText = stringResource(id = R.string.settings_note_translation_provider),
        supportText = providerLabel(state.provider),
        onClick = { pickerVisible = true },
    )
    if (pickerVisible) {
        TranslationChoiceSheet(
            title = stringResource(id = R.string.settings_note_translation_provider),
            options = NoteTranslationProvider.entries.map { it.name to providerLabel(it) },
            selectedKey = state.provider.name,
            onSelect = { key ->
                NoteTranslationProvider.entries
                    .firstOrNull { it.name == key }
                    ?.let { eventPublisher(UiEvent.UpdateProvider(provider = it)) }
            },
            onDismiss = { pickerVisible = false },
        )
    }
}

@Composable
private fun ProviderFields(
    state: TranslationSettingsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    val showLibreUrl = state.provider == NoteTranslationProvider.PreferOnDevice ||
        state.provider == NoteTranslationProvider.LibreTranslate
    val showApiKey = state.provider == NoteTranslationProvider.DeepL ||
        state.provider == NoteTranslationProvider.Google ||
        state.provider == NoteTranslationProvider.LibreTranslate ||
        state.provider == NoteTranslationProvider.PreferOnDevice

    if (showLibreUrl) {
        TranslationTextField(
            title = stringResource(id = R.string.settings_note_translation_libre_url),
            value = state.libreTranslateUrl,
            placeholder = stringResource(id = R.string.settings_note_translation_libre_url_placeholder),
            keyboardType = KeyboardType.Uri,
            onValueChange = { eventPublisher(UiEvent.UpdateLibreTranslateUrl(url = it)) },
        )
    }
    if (showApiKey) {
        TranslationTextField(
            title = apiKeyTitle(state.provider),
            value = state.apiKey,
            placeholder = stringResource(id = R.string.settings_note_translation_api_key_placeholder),
            keyboardType = KeyboardType.Password,
            visualTransformation = true,
            onValueChange = { eventPublisher(UiEvent.UpdateApiKey(apiKey = it)) },
        )
    }
}

@Composable
private fun StatusAndPolicy(state: TranslationSettingsContract.UiState) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        text = if (state.ready) {
            stringResource(id = R.string.settings_note_translation_status_ready)
        } else {
            stringResource(id = R.string.settings_note_translation_status_not_configured)
        },
        style = AppTheme.typography.bodyMedium,
        color = if (state.ready) {
            AppTheme.colorScheme.secondary
        } else {
            AppTheme.extraColorScheme.onSurfaceVariantAlt1
        },
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        text = stringResource(id = R.string.settings_note_translation_fail_closed_policy),
        style = AppTheme.typography.bodySmall,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
    )
}

@Composable
private fun TranslationTextField(
    title: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
    visualTransformation: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        TextSubSection(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            text = title.uppercase(),
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
            visualTransformation = if (visualTransformation) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = keyboardType,
            ),
        )
    }
}

@Composable
private fun TranslationChoiceSheet(
    title: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { (key, label) ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(key)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        text = label,
                        style = AppTheme.typography.bodyLarge,
                        color = if (key == selectedKey) {
                            AppTheme.colorScheme.secondary
                        } else {
                            AppTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        },
        confirmButton = {
            Text(
                modifier = Modifier.clickable(onClick = onDismiss),
                text = stringResource(id = R.string.settings_network_dialog_dismiss),
                color = AppTheme.colorScheme.secondary,
            )
        },
    )
}

@Composable
private fun providerLabel(provider: NoteTranslationProvider): String =
    when (provider) {
        NoteTranslationProvider.PreferOnDevice ->
            stringResource(id = R.string.settings_note_translation_provider_on_device)
        NoteTranslationProvider.LibreTranslate ->
            stringResource(id = R.string.settings_note_translation_provider_libre)
        NoteTranslationProvider.DeepL ->
            stringResource(id = R.string.settings_note_translation_provider_deepl)
        NoteTranslationProvider.Google ->
            stringResource(id = R.string.settings_note_translation_provider_google)
    }

@Composable
private fun apiKeyTitle(provider: NoteTranslationProvider): String =
    when (provider) {
        NoteTranslationProvider.DeepL -> stringResource(id = R.string.settings_note_translation_deepl_key)
        NoteTranslationProvider.Google -> stringResource(id = R.string.settings_note_translation_google_key)
        else -> stringResource(id = R.string.settings_note_translation_optional_api_key)
    }

private fun languageOptions(): List<Pair<String, String>> {
    val device = NoteTranslationSettings.DEVICE_LANGUAGE_CODE to
        NoteTranslationLanguages.displayName(NoteTranslationSettings.DEVICE_LANGUAGE_CODE)
    return listOf(device) + NoteTranslationLanguages.supported.map { it.code to it.englishName }
}
