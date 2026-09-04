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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.theme.AppTheme

@Composable
fun NoteTranslationControls(
    sourceText: String,
    modifier: Modifier = Modifier,
    contentColor: Color = AppTheme.colorScheme.onSurface,
    actionColor: Color = AppTheme.colorScheme.secondary,
) {
    if (!sourceText.hasTranslatableText()) return

    val context = LocalContext.current
    val repository = remember(context) { NoteTranslationRepository(context.applicationContext) }
    val settings = remember(sourceText) { repository.getSettings() }
    if (!settings.enabled) return

    val coroutineScope = rememberCoroutineScope()
    var translationState by remember(sourceText, settings.endpoint, settings.targetLanguage) {
        mutableStateOf<TranslationUiState>(TranslationUiState.Idle)
    }

    val displaySettings = LocalContentDisplaySettings.current

    Column(modifier = modifier.padding(top = 2.dp, bottom = 4.dp)) {
        Text(
            modifier = Modifier.clickable(enabled = translationState !is TranslationUiState.Loading) {
                when (val state = translationState) {
                    TranslationUiState.Idle,
                    is TranslationUiState.Error,
                    -> {
                        translationState = TranslationUiState.Loading
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                repository.translate(sourceText)
                            }
                            translationState = result.fold(
                                onSuccess = { TranslationUiState.Success(translation = it) },
                                onFailure = { TranslationUiState.Error },
                            )
                        }
                    }

                    is TranslationUiState.Success -> {
                        translationState = state.copy(showOriginal = !state.showOriginal)
                    }

                    TranslationUiState.Loading -> Unit
                }
            },
            text = when (val state = translationState) {
                TranslationUiState.Idle -> stringResource(id = R.string.feed_note_translate)
                TranslationUiState.Loading -> stringResource(id = R.string.feed_note_translation_loading)
                is TranslationUiState.Success -> if (state.showOriginal) {
                    stringResource(id = R.string.feed_note_translation_show_translation)
                } else {
                    stringResource(id = R.string.feed_note_translation_show_original)
                }

                is TranslationUiState.Error -> stringResource(id = R.string.feed_note_translation_error)
            },
            style = AppTheme.typography.bodySmall,
            color = actionColor,
        )

        val successState = translationState as? TranslationUiState.Success
        if (successState != null) {
            val translation = successState.translation
            translation.detectedLanguage?.let { detectedLanguage ->
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = stringResource(
                        id = R.string.feed_note_translation_detected_language,
                        detectedLanguage.uppercase(),
                    ),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = if (successState.showOriginal) sourceText else translation.translatedText,
                style = AppTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    fontSize = displaySettings.contentAppearance.noteBodyFontSize,
                    lineHeight = displaySettings.contentAppearance.noteBodyLineHeight,
                ),
            )
        }
    }
}

private sealed interface TranslationUiState {
    data object Idle : TranslationUiState
    data object Loading : TranslationUiState
    data object Error : TranslationUiState
    data class Success(
        val translation: NoteTranslation,
        val showOriginal: Boolean = false,
    ) : TranslationUiState
}

private fun String.hasTranslatableText(): Boolean {
    return length >= MIN_TRANSLATABLE_LENGTH && any { it.isLetter() }
}

private const val MIN_TRANSLATABLE_LENGTH = 12
