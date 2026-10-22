package net.primal.android.settings.content

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.notes.feed.note.translation.LibreTranslateClient
import net.primal.android.notes.feed.note.translation.NoteTranslationPreferences
import net.primal.android.settings.content.ContentDisplaySettingsContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.ContentDisplaySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDisplaySettingsScreen(viewModel: ContentDisplaySettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()

    ContentDisplaySettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@Composable
@ExperimentalMaterial3Api
private fun ContentDisplaySettingsScreen(
    state: ContentDisplaySettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    PrimalScaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_content_display_title),
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
                    headlineText = stringResource(id = R.string.settings_content_display_auto_play_videos),
                    supportText = stringResource(id = R.string.settings_content_display_auto_play_videos_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.autoPlayVideos == ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS,
                            onCheckedChange = {
                                eventPublisher(
                                    UiEvent.UpdateAutoPlayVideos(
                                        code = if (it) {
                                            ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS
                                        } else {
                                            ContentDisplaySettings.AUTO_PLAY_VIDEO_NEVER
                                        },
                                    ),
                                )
                            },
                        )
                    },
                    onClick = {
                        eventPublisher(
                            UiEvent.UpdateAutoPlayVideos(
                                code = if (state.autoPlayVideos != ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS) {
                                    ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS
                                } else {
                                    ContentDisplaySettings.AUTO_PLAY_VIDEO_NEVER
                                },
                            ),
                        )
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_content_display_animated_avatars),
                    supportText = stringResource(id = R.string.settings_content_display_animated_avatars_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.showAnimatedAvatars,
                            onCheckedChange = {
                                eventPublisher(UiEvent.UpdateShowAnimatedAvatars(enabled = it))
                            },
                        )
                    },
                    onClick = {
                        eventPublisher(UiEvent.UpdateShowAnimatedAvatars(enabled = !state.showAnimatedAvatars))
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_content_display_full_screen_feed_display),
                    supportText = stringResource(id = R.string.settings_content_display_full_screen_feed_display_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.focusMode,
                            onCheckedChange = {
                                eventPublisher(UiEvent.UpdateShowFocusMode(enabled = it))
                            },
                        )
                    },
                    onClick = {
                        eventPublisher(UiEvent.UpdateShowFocusMode(enabled = !state.focusMode))
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))
                NoteTranslationSettingsSection()
            }
        },
    )
}

@Composable
private fun NoteTranslationSettingsSection() {
    val context = LocalContext.current.applicationContext
    var enabled by remember { mutableStateOf(NoteTranslationPreferences.isEnabled(context)) }
    var baseUrl by remember { mutableStateOf(NoteTranslationPreferences.baseUrl(context)) }
    var apiKey by remember { mutableStateOf(NoteTranslationPreferences.apiKey(context).orEmpty()) }
    var targetLang by remember { mutableStateOf(NoteTranslationPreferences.targetLanguage(context)) }

    Text(
        text = stringResource(id = R.string.settings_content_display_translation_section),
        style = AppTheme.typography.titleMedium,
        color = AppTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )

    Spacer(modifier = Modifier.height(8.dp))

    SettingsItem(
        headlineText = stringResource(id = R.string.settings_content_display_note_translation),
        supportText = stringResource(id = R.string.settings_content_display_note_translation_hint),
        trailingContent = {
            PrimalSwitch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    NoteTranslationPreferences.setEnabled(context, it)
                },
            )
        },
        onClick = {
            enabled = !enabled
            NoteTranslationPreferences.setEnabled(context, enabled)
        },
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                NoteTranslationPreferences.setBaseUrl(context, it.ifBlank { LibreTranslateClient.DEFAULT_BASE_URL })
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(id = R.string.settings_content_display_translation_endpoint)) },
            supportingText = {
                Text(stringResource(id = R.string.settings_content_display_translation_endpoint_hint))
            },
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                NoteTranslationPreferences.setApiKey(context, it)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            label = { Text(stringResource(id = R.string.settings_content_display_translation_api_key)) },
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = targetLang,
            onValueChange = {
                targetLang = it
                NoteTranslationPreferences.setTargetLanguage(context, it)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(id = R.string.settings_content_display_translation_language)) },
            supportingText = {
                Text(stringResource(id = R.string.settings_content_display_translation_language_hint))
            },
            enabled = enabled,
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
}
