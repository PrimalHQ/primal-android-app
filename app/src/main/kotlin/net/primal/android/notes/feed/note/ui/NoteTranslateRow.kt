package net.primal.android.notes.feed.note.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.translation.NoteTranslationPrefsStore
import net.primal.android.translation.NoteTranslationService
import net.primal.android.translation.TranslationSanitizer

@Composable
fun NoteTranslateRow(
    noteId: String,
    noteContent: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { NoteTranslationPrefsStore.read(context) }
    var tick by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(noteId) {
        val unsub = NoteTranslationService.subscribe { id ->
            if (id == noteId) tick += 1
        }
        onDispose { unsub() }
    }

    val entry = remember(tick, noteId) { NoteTranslationService.getCached(noteId) }
    if (!prefs.enabled || !TranslationSanitizer.shouldOfferTranslation(noteContent)) {
        return
    }

    val actionLabel = when {
        busy -> stringResource(id = R.string.feed_note_translating)
        entry != null && !entry.showingOriginal -> stringResource(id = R.string.feed_note_show_original)
        entry != null && entry.showingOriginal -> stringResource(id = R.string.feed_note_show_translation)
        else -> stringResource(id = R.string.feed_note_translate)
    }

    Column(modifier = modifier.fillMaxWidth().padding(top = 6.dp)) {
        Text(
            text = actionLabel,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.primary,
            modifier = Modifier.clickable(enabled = !busy) {
                scope.launch {
                    error = null
                    try {
                        val cached = NoteTranslationService.getCached(noteId)
                        if (cached != null) {
                            NoteTranslationService.toggleShowOriginal(noteId)
                        } else {
                            busy = true
                            NoteTranslationService.translate(noteId, noteContent, prefs)
                        }
                    } catch (t: Throwable) {
                        error = t.message ?: "Translation failed"
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    } finally {
                        busy = false
                    }
                }
            },
        )

        if (entry != null && !entry.showingOriginal) {
            Text(
                text = entry.translated,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                modifier = Modifier.padding(top = 4.dp),
            )
            entry.detected?.let { detected ->
                Text(
                    text = stringResource(id = R.string.feed_note_detected_language, detected),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        error?.let {
            Text(
                text = it,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
