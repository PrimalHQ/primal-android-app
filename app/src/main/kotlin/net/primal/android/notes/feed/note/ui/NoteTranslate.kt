package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun NoteTranslate(
    modifier: Modifier = Modifier,
    noteId: String,
    content: String,
) {
    if (content.isBlank()) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var translation by remember(noteId) { mutableStateOf<TranslationResult?>(null) }
    var translating by remember(noteId) { mutableStateOf(false) }
    var failed by remember(noteId) { mutableStateOf(false) }

    val translate = {
        if (!translating) {
            val target = TranslationPreferences.getTranslateLanguage(context)
            val cached = NoteTranslator.getCachedTranslation(noteId, target)
            if (cached != null) {
                translation = cached
            } else {
                translating = true
                failed = false
                scope.launch {
                    val result = NoteTranslator.translateNoteContent(content, target)
                    translating = false
                    if (result != null) {
                        NoteTranslator.cacheTranslation(noteId, result)
                        translation = result
                    } else {
                        failed = true
                    }
                }
            }
        }
    }

    val translated = translation

    Column(modifier = modifier.fillMaxWidth()) {
        when {
            translated != null -> {
                Text(
                    text = translated.text,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            id = if (translated.engine == TranslationEngine.GOOGLE) {
                                R.string.note_translated_by_google
                            } else {
                                R.string.note_translated_by_libretranslate
                            },
                        ),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            translation = null
                            failed = false
                        },
                    ) {
                        Text(
                            text = stringResource(id = R.string.note_show_original),
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colorScheme.primary,
                        )
                    }
                }
            }

            failed -> {
                Text(
                    text = stringResource(id = R.string.note_translation_unavailable),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            }

            else -> {
                TextButton(
                    onClick = translate,
                    enabled = !translating,
                ) {
                    Text(
                        text = stringResource(
                            id = if (translating) R.string.note_translating else R.string.note_translate,
                        ),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
