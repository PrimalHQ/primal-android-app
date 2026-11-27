package net.primal.android.settings.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Check
import net.primal.android.core.compose.settings.DecoratedSettingsOutlinedTextField
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.settings.translation.TranslationSettingsContract.TestOutcome
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsScreen(viewModel: TranslationSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()

    TranslationSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@Composable
@ExperimentalMaterial3Api
private fun TranslationSettingsScreen(
    state: TranslationSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    PrimalScaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_translation_title),
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
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_translation_enable),
                    supportText = stringResource(id = R.string.settings_translation_enable_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.enabled,
                            onCheckedChange = { eventPublisher(UiEvent.UpdateEnabled(enabled = it)) },
                        )
                    },
                    onClick = { eventPublisher(UiEvent.UpdateEnabled(enabled = !state.enabled)) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                DecoratedSettingsOutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.settings_translation_endpoint),
                    placeholderText = stringResource(id = R.string.settings_translation_endpoint_placeholder),
                    value = state.endpointDraft,
                    onValueChanged = { eventPublisher(UiEvent.UpdateEndpointDraft(value = it)) },
                    buttonEnabled = state.endpointDraft.isNotBlank() && state.endpointDraft != state.endpoint,
                    onActionClick = { eventPublisher(UiEvent.ConfirmEndpoint) },
                )

                Spacer(modifier = Modifier.height(8.dp))

                DecoratedSettingsOutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.settings_translation_api_key),
                    placeholderText = stringResource(id = R.string.settings_translation_api_key_placeholder),
                    value = state.apiKeyDraft,
                    onValueChanged = { eventPublisher(UiEvent.UpdateApiKeyDraft(value = it)) },
                    buttonEnabled = state.apiKeyDraft != state.apiKey,
                    onActionClick = { eventPublisher(UiEvent.ConfirmApiKey) },
                    visualTransformation = PasswordVisualTransformation(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                DecoratedSettingsOutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.settings_translation_target_language),
                    placeholderText = stringResource(id = R.string.settings_translation_target_language_placeholder),
                    value = state.targetLanguageDraft,
                    onValueChanged = { eventPublisher(UiEvent.UpdateTargetLanguageDraft(value = it)) },
                    buttonEnabled = state.targetLanguageDraft.isNotBlank() &&
                        state.targetLanguageDraft != state.targetLanguage,
                    onActionClick = { eventPublisher(UiEvent.ConfirmTargetLanguage) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_translation_test_connection),
                    supportText = testOutcomeText(state.testOutcome),
                    trailingContent = {
                        if (state.testingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    },
                    onClick = { eventPublisher(UiEvent.TestConnection) },
                )

                if (state.languages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    state.languages.forEach { language ->
                        SettingsItem(
                            headlineText = language.name,
                            supportText = language.code,
                            trailingContent = {
                                if (language.code == state.targetLanguage) {
                                    Icon(
                                        imageVector = PrimalIcons.Check,
                                        contentDescription = null,
                                        tint = AppTheme.colorScheme.primary,
                                    )
                                }
                            },
                            onClick = { eventPublisher(UiEvent.SelectLanguage(code = language.code)) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_translation_cache),
                    supportText = stringResource(
                        id = R.string.settings_translation_cache_count,
                        state.cacheCount,
                    ),
                    trailingContent = {
                        TextButton(
                            onClick = { eventPublisher(UiEvent.ClearCache) },
                            enabled = state.cacheCount > 0,
                        ) {
                            Text(text = stringResource(id = R.string.settings_translation_cache_clear))
                        }
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.settings_translation_privacy_note),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        },
    )
}

@Composable
private fun testOutcomeText(outcome: TestOutcome?): String? {
    return when (outcome) {
        null -> stringResource(id = R.string.settings_translation_test_hint)
        TestOutcome.Success -> stringResource(id = R.string.settings_translation_test_success)
        TestOutcome.NotConfigured -> stringResource(id = R.string.settings_translation_test_not_configured)
        TestOutcome.Unreachable -> stringResource(id = R.string.settings_translation_test_unreachable)
        TestOutcome.ProviderError -> stringResource(id = R.string.settings_translation_test_error)
    }
}
