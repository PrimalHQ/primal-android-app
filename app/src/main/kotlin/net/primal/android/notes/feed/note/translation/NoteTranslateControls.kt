package net.primal.android.notes.feed.note.translation

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.theme.AppTheme

private sealed interface TranslateUiState {
    data object Idle : TranslateUiState
    data object Loading : TranslateUiState
    data class Ready(
        val text: String,
        val showingTranslation: Boolean,
        val caption: String?,
    ) : TranslateUiState
    data class Error(val messageRes: Int) : TranslateUiState
}

@Composable
fun NoteTranslateControls(
    noteText: String,
    modifier: Modifier = Modifier,
    libreTranslateBaseUrl: String? = null,
) {
    if (noteText.isBlank()) return

    val context = LocalContext.current
    val appContext = context.applicationContext
    if (!NoteTranslationPreferences.isEnabled(appContext)) return

    var state by remember(noteText) { mutableStateOf<TranslateUiState>(TranslateUiState.Idle) }
    val scope = rememberCoroutineScope()
    val accent = AppTheme.colorScheme.secondary
    val onDeviceCaption = stringResource(id = R.string.note_translation_via_on_device)
    val networkCaption = stringResource(id = R.string.note_translation_via_network)
    val resolvedBaseUrl = libreTranslateBaseUrl ?: NoteTranslationPreferences.baseUrl(appContext)

    Column(modifier = modifier.padding(top = 6.dp)) {
        when (val s = state) {
            TranslateUiState.Idle -> {
                Text(
                    text = stringResource(id = R.string.note_translation_action),
                    color = accent,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier.clickable {
                        state = TranslateUiState.Loading
                        scope.launch {
                            state = translateNote(
                                noteText = noteText,
                                context = appContext,
                                libreTranslateBaseUrl = resolvedBaseUrl,
                                onDeviceCaption = onDeviceCaption,
                                networkCaption = networkCaption,
                            )
                        }
                    },
                )
            }
            TranslateUiState.Loading -> {
                Text(
                    text = stringResource(id = R.string.note_translation_translating),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodySmall,
                )
            }
            is TranslateUiState.Ready -> {
                if (s.showingTranslation) {
                    Text(
                        text = s.text,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                    s.caption?.let { caption ->
                        Text(
                            text = caption,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.note_translation_show_original),
                        color = accent,
                        style = AppTheme.typography.bodySmall,
                        modifier = Modifier.clickable {
                            state = s.copy(showingTranslation = false)
                        },
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.note_translation_show_translation),
                        color = accent,
                        style = AppTheme.typography.bodySmall,
                        modifier = Modifier.clickable {
                            state = s.copy(showingTranslation = true)
                        },
                    )
                }
            }
            is TranslateUiState.Error -> {
                Text(
                    text = stringResource(id = s.messageRes),
                    color = AppTheme.colorScheme.error,
                    style = AppTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(id = R.string.note_translation_retry),
                    color = accent,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { state = TranslateUiState.Idle },
                )
            }
        }
    }
}

private suspend fun translateNote(
    noteText: String,
    context: android.content.Context,
    libreTranslateBaseUrl: String,
    onDeviceCaption: String,
    networkCaption: String,
): TranslateUiState {
    // Prefer private on-device translation when available (API 31+).
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            val result = SystemNoteTranslator(context).translate(noteText)
            return TranslateUiState.Ready(
                text = result.text,
                showingTranslation = true,
                caption = "$onDeviceCaption (${result.targetLanguage})",
            )
        } catch (_: NoteTranslationException.AlreadyInTargetLanguage) {
            return TranslateUiState.Error(R.string.note_translation_already_in_target)
        } catch (_: NoteTranslationException) {
            // Fall through to LibreTranslate.
        } catch (_: Throwable) {
            // Fall through to LibreTranslate.
        }
    }

    return runCatching {
        val protected = NoteTextSanitizer.protect(noteText)
        val target = NoteTranslationPreferences.targetLanguage(context)
        val apiKey = NoteTranslationPreferences.apiKey(context)
        val raw = withContext(Dispatchers.IO) {
            LibreTranslateClient(
                baseUrl = libreTranslateBaseUrl,
                apiKey = apiKey,
            ).translate(protected.text, targetLang = target)
        }
        val restored = NoteTextSanitizer.restore(raw, protected.tokens)
        TranslateUiState.Ready(
            text = restored,
            showingTranslation = true,
            caption = networkCaption,
        )
    }.getOrElse {
        TranslateUiState.Error(R.string.note_translation_failed)
    }
}
