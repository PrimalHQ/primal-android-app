package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.notes.translation.NoteTokenProtector
import net.primal.android.notes.translation.NoteTranslationEngine
import net.primal.android.notes.translation.NoteTranslationHostViewModel
import net.primal.android.notes.translation.NoteTranslationOutcome
import net.primal.android.notes.translation.NoteTranslationSource
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.NoteTranslationSettings

@Composable
fun NoteTranslationControls(
    noteId: String,
    sourceText: String,
    onTranslatedContentChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) return
    val settings = LocalContentDisplaySettings.current.noteTranslation
    if (!settings.enabled || !NoteTokenProtector.shouldOfferTranslation(sourceText)) {
        LaunchedEffect(noteId) { onTranslatedContentChange(null) }
        return
    }

    val viewModel = hiltViewModel<NoteTranslationHostViewModel>()
    NoteTranslationControlsContent(
        modifier = modifier,
        noteId = noteId,
        sourceText = sourceText,
        settings = settings,
        engine = viewModel.engine,
        onTranslatedContentChange = onTranslatedContentChange,
    )
}

@Composable
private fun NoteTranslationControlsContent(
    noteId: String,
    sourceText: String,
    settings: NoteTranslationSettings,
    engine: NoteTranslationEngine,
    onTranslatedContentChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var uiState by remember(noteId, sourceText, settings) {
        mutableStateOf<NoteTranslationUiState>(NoteTranslationUiState.Idle)
    }

    LaunchedEffect(noteId, sourceText) {
        onTranslatedContentChange(null)
        uiState = NoteTranslationUiState.Idle
    }

    Column(modifier = modifier.padding(top = 2.dp, bottom = 2.dp)) {
        when (val state = uiState) {
            NoteTranslationUiState.Hidden -> Unit
            NoteTranslationUiState.Idle ->
                TranslationActionText(
                    text = stringResource(id = R.string.feed_note_translate),
                    onClick = {
                        uiState = NoteTranslationUiState.Loading
                        scope.launch {
                            uiState = engine.translate(text = sourceText, settings = settings).toUiState()
                            val showing = uiState as? NoteTranslationUiState.Showing
                            onTranslatedContentChange(showing?.text)
                        }
                    },
                )

            NoteTranslationUiState.Loading ->
                TranslationCaption(text = stringResource(id = R.string.feed_note_translating))

            is NoteTranslationUiState.Showing -> {
                TranslationCaption(text = sourceCaption(state.source))
                TranslationActionText(
                    text = stringResource(id = R.string.feed_note_show_original),
                    onClick = {
                        onTranslatedContentChange(null)
                        uiState = NoteTranslationUiState.Idle
                    },
                )
            }

            NoteTranslationUiState.AlreadyInTarget ->
                TranslationCaption(text = stringResource(id = R.string.feed_note_already_in_language))

            NoteTranslationUiState.NotConfigured ->
                TranslationCaption(text = stringResource(id = R.string.feed_note_translation_not_configured))

            NoteTranslationUiState.Error -> {
                TranslationCaption(text = stringResource(id = R.string.feed_note_translation_failed))
                TranslationActionText(
                    text = stringResource(id = R.string.feed_note_translation_retry),
                    onClick = { uiState = NoteTranslationUiState.Idle },
                )
            }
        }
    }
}

@Composable
private fun sourceCaption(source: NoteTranslationSource): String =
    when (source) {
        NoteTranslationSource.OnDevice -> stringResource(id = R.string.feed_note_translated_on_device)
        NoteTranslationSource.LibreTranslate -> stringResource(id = R.string.feed_note_translated_libre)
        NoteTranslationSource.DeepL -> stringResource(id = R.string.feed_note_translated_deepl)
        NoteTranslationSource.Google -> stringResource(id = R.string.feed_note_translated_google)
    }

@Composable
private fun TranslationActionText(text: String, onClick: () -> Unit) {
    Text(
        modifier = Modifier.clickable(onClick = onClick),
        text = text,
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.secondary,
    )
}

@Composable
private fun TranslationCaption(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
    )
}

private sealed class NoteTranslationUiState {
    data object Hidden : NoteTranslationUiState()
    data object Idle : NoteTranslationUiState()
    data object Loading : NoteTranslationUiState()
    data class Showing(val text: String, val source: NoteTranslationSource) : NoteTranslationUiState()
    data object AlreadyInTarget : NoteTranslationUiState()
    data object NotConfigured : NoteTranslationUiState()
    data object Error : NoteTranslationUiState()
}

private fun NoteTranslationOutcome.toUiState(): NoteTranslationUiState =
    when (this) {
        is NoteTranslationOutcome.Translated -> NoteTranslationUiState.Showing(text = text, source = source)
        is NoteTranslationOutcome.AlreadyInTarget -> NoteTranslationUiState.AlreadyInTarget
        NoteTranslationOutcome.NotConfigured -> NoteTranslationUiState.NotConfigured
        is NoteTranslationOutcome.Failed -> NoteTranslationUiState.Error
    }
