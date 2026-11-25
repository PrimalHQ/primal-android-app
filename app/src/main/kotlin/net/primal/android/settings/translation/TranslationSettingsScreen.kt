package net.primal.android.settings.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.translation.TranslationService
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.settings.translation.TranslationSettingsContract.UiState
import net.primal.android.theme.AppTheme

@Composable
fun TranslationSettingsScreen(viewModel: TranslationSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()

    TranslationSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslationSettingsScreen(
    state: UiState,
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
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Info section
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = AppTheme.colorScheme.surface,
                    ),
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.settings_translation_about),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(id = R.string.settings_translation_description),
                            style = AppTheme.typography.bodyMedium,
                        )
                    },
                )

                PrimalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Target language section
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = AppTheme.colorScheme.surfaceVariant,
                    ),
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.settings_translation_target_language),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        )
                    },
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(
                        items = state.languages,
                        key = { it.code },
                    ) { language ->
                        LanguageItem(
                            language = language,
                            isSelected = language.code == state.selectedLanguageCode,
                            onClick = {
                                eventPublisher(UiEvent.SelectLanguage(language.code))
                            },
                        )
                        PrimalDivider()
                    }
                }
            }
        },
    )
}

@Composable
private fun LanguageItem(
    language: TranslationService.Language,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surface,
        ),
        headlineContent = {
            Text(
                text = language.name,
                style = AppTheme.typography.bodyLarge,
            )
        },
        trailingContent = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AppTheme.colorScheme.primary,
                )
            }
        } else {
            null
        },
    )
}

@Preview
@Composable
fun PreviewTranslationSettingsScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        TranslationSettingsScreen(
            state = UiState(
                selectedLanguageCode = "en",
                languages = TranslationService.supportedLanguages,
            ),
            onClose = {},
            eventPublisher = {},
        )
    }
}
