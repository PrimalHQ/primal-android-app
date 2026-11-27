package net.primal.android.notes.translation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.notes.translation.LanguageDetector
import net.primal.android.notes.translation.TranslationCache
import net.primal.android.notes.translation.TranslationErrorCode
import net.primal.android.notes.translation.TranslationException
import net.primal.android.notes.translation.TranslationResult
import net.primal.android.notes.translation.TranslationSanitizer
import net.primal.android.notes.translation.rememberNoteTranslationManager
import net.primal.android.theme.AppTheme

private sealed interface NoteTranslationUiState {
    data object Idle : NoteTranslationUiState
    data object Loading : NoteTranslationUiState
    data class Translated(val result: TranslationResult, val showTranslation: Boolean = true) : NoteTranslationUiState
    data class Error(val code: TranslationErrorCode) : NoteTranslationUiState
}

/**
 * Inline translation controls rendered directly below a note's content.
 *
 * Renders nothing unless the feature is enabled in Settings and the note is
 * eligible (long enough, has prose, and does not already appear to be in the
 * user's target language). Translation runs only on an explicit tap — never
 * automatically — and the result is shown under the original text with a
 * toggle to hide it again, mirroring the web implementation (PR #240).
 */
@Composable
fun NoteTranslateBar(
    noteId: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    val manager = rememberNoteTranslationManager()
    val settings by manager.settingsStore.settingsFlow.collectAsState()

    val targetLanguage = LanguageDetector.primaryLanguageCode(settings.targetLanguage)
    val eligible = remember(content, settings.enabled, targetLanguage) {
        when {
            !settings.enabled -> false
            !TranslationSanitizer.shouldOfferTranslation(content) -> false
            targetLanguage == null -> true
            else -> {
                val detected = LanguageDetector.detectLanguage(
                    TranslationSanitizer.extractTranslatableProse(content),
                )
                detected == null || detected != targetLanguage
            }
        }
    }
    if (!eligible) return

    // The displayed result is only valid for the note AND the settings it was
    // produced with; changing endpoint, target language or API key resets
    // the bar. The API key itself is never retained in the key — only a
    // hash of it, which is all change detection needs.
    val stateKey = "$noteId|$content|${settings.endpoint}|${settings.targetLanguage}|${TranslationCache.cyrb53(settings.apiKey)}"
    var state by remember(stateKey) { mutableStateOf<NoteTranslationUiState>(NoteTranslationUiState.Idle) }
    val scope = rememberCoroutineScope()
    var job by remember(stateKey) { mutableStateOf<Job?>(null) }
    // Monotonic request counter: a superseded job can never overwrite the
    // state of a newer request, even if it ignores cancellation.
    var generation by remember { mutableStateOf(0) }

    // When the key changes (different note or settings), cancel the previous
    // request outright instead of letting it finish in the background.
    // rememberUpdatedState guarantees the dispose block sees the latest job.
    val currentJob by rememberUpdatedState(job)
    DisposableEffect(stateKey) {
        onDispose { currentJob?.cancel() }
    }

    fun startTranslation() {
        job?.cancel()
        generation += 1
        val requestGeneration = generation
        val keyAtStart = stateKey
        state = NoteTranslationUiState.Loading
        job = scope.launch {
            try {
                val result = manager.translateNote(content)
                if (stateKey == keyAtStart && generation == requestGeneration) {
                    state = NoteTranslationUiState.Translated(result)
                }
            } catch (e: CancellationException) {
                if (stateKey == keyAtStart && generation == requestGeneration) {
                    state = NoteTranslationUiState.Idle
                }
            } catch (e: TranslationException) {
                if (stateKey == keyAtStart && generation == requestGeneration) {
                    state = NoteTranslationUiState.Error(e.code)
                }
            } catch (e: Exception) {
                if (stateKey == keyAtStart && generation == requestGeneration) {
                    state = NoteTranslationUiState.Error(TranslationErrorCode.ProviderError)
                }
            }
        }
    }

    Column(modifier = modifier) {
        when (val current = state) {
            NoteTranslationUiState.Idle -> {
                TextButton(onClick = { startTranslation() }) {
                    Text(text = stringResource(id = R.string.note_translation_translate))
                }
            }

            NoteTranslationUiState.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.note_translation_translating),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = {
                            job?.cancel()
                            state = NoteTranslationUiState.Idle
                        },
                    ) {
                        Text(text = stringResource(id = R.string.note_translation_cancel))
                    }
                }
            }

            is NoteTranslationUiState.Translated -> {
                val sourceCode = current.result.detectedLanguage ?: current.result.localDetectedLanguage
                Text(
                    text = if (sourceCode != null) {
                        stringResource(
                            id = R.string.note_translation_translated_from,
                            languageDisplayName(sourceCode),
                        )
                    } else {
                        stringResource(id = R.string.note_translation_translated)
                    },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (current.showTranslation) {
                    Text(
                        text = current.result.translatedText,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colorScheme.onSurface,
                    )
                }
                TextButton(
                    onClick = {
                        state = current.copy(showTranslation = !current.showTranslation)
                    },
                ) {
                    Text(
                        text = stringResource(
                            id = if (current.showTranslation) {
                                R.string.note_translation_hide_translation
                            } else {
                                R.string.note_translation_show_translation
                            },
                        ),
                    )
                }
            }

            is NoteTranslationUiState.Error -> {
                Text(
                    text = stringResource(id = errorMessageRes(current.code)),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colorScheme.error,
                )
                TextButton(onClick = { startTranslation() }) {
                    Text(text = stringResource(id = R.string.note_translation_retry))
                }
            }
        }
    }
}

private fun languageDisplayName(code: String): String {
    val locale = Locale.forLanguageTag(code)
    val display = locale.getDisplayLanguage(Locale.getDefault())
    return display.ifEmpty { code }
}

private fun errorMessageRes(code: TranslationErrorCode): Int {
    return when (code) {
        TranslationErrorCode.Disabled -> R.string.note_translation_error_generic
        TranslationErrorCode.NotConfigured -> R.string.note_translation_error_not_configured
        TranslationErrorCode.EmptyContent -> R.string.note_translation_error_empty
        TranslationErrorCode.Unreachable -> R.string.note_translation_error_unreachable
        TranslationErrorCode.ProviderError -> R.string.note_translation_error_provider
    }
}
