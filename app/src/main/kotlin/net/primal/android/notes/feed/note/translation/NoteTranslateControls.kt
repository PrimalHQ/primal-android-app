package net.primal.android.notes.feed.note.translation

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
    data class Ready(val text: String, val showingTranslation: Boolean) : TranslateUiState
    data class Error(val message: String) : TranslateUiState
}

@Composable
fun NoteTranslateControls(
    noteText: String,
    modifier: Modifier = Modifier,
    libreTranslateBaseUrl: String = LibreTranslateClient.DEFAULT_BASE_URL,
) {
    if (noteText.isBlank()) return

    var state by remember(noteText) { mutableStateOf<TranslateUiState>(TranslateUiState.Idle) }
    val scope = rememberCoroutineScope()
    val accent = AppTheme.colorScheme.secondary

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
                            state = runCatching {
                                val protected = NoteTextSanitizer.protect(noteText)
                                val target = LibreTranslateClient.deviceLanguageCode()
                                val raw = withContext(Dispatchers.IO) {
                                    LibreTranslateClient(baseUrl = libreTranslateBaseUrl)
                                        .translate(protected.text, targetLang = target)
                                }
                                val restored = NoteTextSanitizer.restore(raw, protected.tokens)
                                TranslateUiState.Ready(text = restored, showingTranslation = true)
                            }.getOrElse {
                                TranslateUiState.Error(
                                    it.message ?: "translation failed",
                                )
                            }
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
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
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
                    text = stringResource(id = R.string.note_translation_failed),
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
