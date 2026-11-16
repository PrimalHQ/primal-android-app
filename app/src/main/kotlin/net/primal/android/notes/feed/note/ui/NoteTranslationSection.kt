package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedTranslate
import net.primal.android.notes.translation.NoteTranslationService
import net.primal.android.theme.AppTheme

/**
 * Inline note translation shown in the note footer, above the actions row.
 *
 * Renders a "Translate" action; once tapped, the note is translated into
 * the user's language (device/app locale) via [NoteTranslationService] and
 * the result is shown inline with a "Translated from {language}" header
 * and a Show/Hide translation toggle. Translation failures degrade
 * gracefully to a subtle error hint and the action can be retried.
 */
@Composable
fun NoteTranslationSection(
    content: String,
    modifier: Modifier = Modifier,
    translationService: NoteTranslationService = remember { NoteTranslationService() },
) {
    if (content.isBlank()) return

    val scope = rememberCoroutineScope()
    val targetLanguage = remember { Locale.getDefault().toLanguageTag() }

    var translationText by remember(content) { mutableStateOf<String?>(null) }
    var detectedLanguage by remember(content) { mutableStateOf<String?>(null) }
    var translationVisible by remember(content) { mutableStateOf(false) }
    var isTranslating by remember(content) { mutableStateOf(false) }
    var translationFailed by remember(content) { mutableStateOf(false) }

    val translatedText = translationText
    val sourceLanguage = detectedLanguage

    Column(modifier = modifier) {
        if (translatedText == null || sourceLanguage == null) {
            Row(
                modifier = Modifier
                    .clickable(enabled = !isTranslating) {
                        isTranslating = true
                        translationFailed = false
                        scope.launch {
                            val result = translationService.translate(
                                content = content,
                                targetLanguage = targetLanguage,
                            )
                            translationText = result?.text
                            detectedLanguage = result?.detectedLanguage
                            translationVisible = result != null
                            translationFailed = result == null
                            isTranslating = false
                        }
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = AppTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        imageVector = PrimalIcons.FeedTranslate,
                        contentDescription = stringResource(id = R.string.accessibility_translate_note),
                        modifier = Modifier.size(16.dp),
                        tint = AppTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.note_translate_action),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.primary,
                )
            }

            if (translationFailed) {
                Text(
                    text = stringResource(id = R.string.note_translate_failed),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clickable { translationVisible = !translationVisible }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = PrimalIcons.FeedTranslate,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(
                        id = R.string.note_translated_from,
                        languageDisplayName(sourceLanguage),
                    ),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        id = if (translationVisible) {
                            R.string.note_translate_hide
                        } else {
                            R.string.note_translate_show
                        },
                    ),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                )
            }

            if (translationVisible) {
                Text(
                    text = translatedText,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private fun languageDisplayName(languageCode: String): String {
    val displayLanguage = Locale.forLanguageTag(languageCode)
        .getDisplayLanguage(Locale.getDefault())
    return displayLanguage
        .ifBlank { languageCode }
        .replaceFirstChar { it.uppercase(Locale.getDefault()) }
}
