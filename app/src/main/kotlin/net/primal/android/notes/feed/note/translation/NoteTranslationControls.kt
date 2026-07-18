package net.primal.android.notes.feed.note.translation

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.theme.AppTheme

internal sealed interface NoteTranslationUiState {
    data object Idle : NoteTranslationUiState
    data object Loading : NoteTranslationUiState
    data class Success(val translation: TranslatedNote) : NoteTranslationUiState
    data class Error(val reason: NoteTranslationException) : NoteTranslationUiState
}

@Composable
internal fun NoteTranslationControls(
    noteId: String,
    text: String,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || text.isBlank()) return

    val context = LocalContext.current
    val translator = remember(context) { SystemNoteTranslator(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var state by remember(noteId, text) { mutableStateOf<NoteTranslationUiState>(NoteTranslationUiState.Idle) }

    Column(modifier = Modifier.fillMaxWidth()) {
        TranslationAction(
            state = state,
            onClick = {
                when (state) {
                    is NoteTranslationUiState.Success -> state = NoteTranslationUiState.Idle
                    NoteTranslationUiState.Loading -> Unit
                    else -> scope.launch {
                        state = NoteTranslationUiState.Loading
                        state = runCatching { translator.translate(text) }.fold(
                            onSuccess = { NoteTranslationUiState.Success(it) },
                            onFailure = {
                                NoteTranslationUiState.Error(
                                    it as? NoteTranslationException ?: NoteTranslationException.Failed,
                                )
                            },
                        )
                    }
                }
            },
        )

        when (val currentState = state) {
            is NoteTranslationUiState.Success -> {
                Text(
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    text = currentState.translation.text,
                    style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colorScheme.onSurface),
                )
                Text(
                    text = stringResource(
                        id = R.string.note_translation_target_language,
                        currentState.translation.targetLanguage,
                    ),
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    ),
                )
            }

            is NoteTranslationUiState.Error -> Text(
                modifier = Modifier.padding(top = 2.dp),
                text = stringResource(id = currentState.reason.messageResource()),
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colorScheme.error),
            )

            else -> Unit
        }
    }
}

@Composable
private fun TranslationAction(
    state: NoteTranslationUiState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(top = 2.dp)
            .clickable(enabled = state != NoteTranslationUiState.Loading, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state == NoteTranslationUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = AppTheme.colorScheme.secondary,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Outlined.Translate,
                contentDescription = null,
                tint = AppTheme.colorScheme.secondary,
            )
        }
        Text(
            text = stringResource(
                id = when (state) {
                    NoteTranslationUiState.Loading -> R.string.note_translation_translating
                    is NoteTranslationUiState.Success -> R.string.note_translation_hide
                    is NoteTranslationUiState.Error -> R.string.note_translation_retry
                    NoteTranslationUiState.Idle -> R.string.note_translation_action
                },
            ),
            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colorScheme.secondary),
        )
    }
}

internal fun NoteTranslationException.messageResource(): Int = when (this) {
    NoteTranslationException.AlreadyInTargetLanguage -> R.string.note_translation_already_in_target
    NoteTranslationException.LanguageNotDetected -> R.string.note_translation_language_not_detected
    NoteTranslationException.Unavailable -> R.string.note_translation_unavailable
    NoteTranslationException.Failed -> R.string.note_translation_failed
}
