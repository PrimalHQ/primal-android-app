package net.primal.android.settings.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.theme.AppTheme
import net.primal.android.translation.NoteTranslationPrefs
import net.primal.android.translation.NoteTranslationPrefsStore
import net.primal.android.translation.TranslationProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(NoteTranslationPrefsStore.read(context)) }

    fun persist(next: NoteTranslationPrefs) {
        prefs = next
        NoteTranslationPrefsStore.write(context, next)
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
            Column(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.settings_translation_caption),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
                Spacer(modifier = Modifier.height(16.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_translation_enabled),
                    supportText = stringResource(id = R.string.settings_translation_enabled_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = prefs.enabled,
                            onCheckedChange = { persist(prefs.copy(enabled = it)) },
                        )
                    },
                    onClick = { persist(prefs.copy(enabled = !prefs.enabled)) },
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.settings_translation_provider),
                    style = AppTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TranslationProvider.entries.forEach { provider ->
                    TextButton(
                        onClick = { persist(prefs.copy(provider = provider)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (prefs.provider == provider) "✓ ${provider.name}" else provider.name,
                            color = if (prefs.provider == provider) {
                                AppTheme.colorScheme.primary
                            } else {
                                AppTheme.extraColorScheme.onSurfaceVariantAlt2
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = prefs.targetLang,
                    onValueChange = { persist(prefs.copy(targetLang = it.trim())) },
                    label = { Text(stringResource(id = R.string.settings_translation_target_lang)) },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = prefs.libreBaseUrl,
                    onValueChange = { persist(prefs.copy(libreBaseUrl = it)) },
                    label = { Text(stringResource(id = R.string.settings_translation_libre_url)) },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = prefs.apiKey,
                    onValueChange = { persist(prefs.copy(apiKey = it)) },
                    label = { Text(stringResource(id = R.string.settings_translation_api_key)) },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        },
    )
}
