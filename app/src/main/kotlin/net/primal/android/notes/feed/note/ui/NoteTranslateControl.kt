package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.notes.translation.NoteTranslate
import net.primal.android.theme.AppTheme

/**
 * X-style inline translate control shown under note body text.
 * Target language follows the app/device locale (no API keys required).
 */
@Composable
fun NoteTranslateControl(
    content: String,
    modifier: Modifier = Modifier,
    settingsLocale: String? = null,
) {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) return

    val configuration = LocalConfiguration.current
    val deviceLocale = remember(configuration) {
        @Suppress("DEPRECATION")
        configuration.locales[0] ?: Locale.getDefault()
    }
    val targetLang = remember(settingsLocale, deviceLocale) {
        NoteTranslate.resolveTargetLanguage(
            settingsLocale = settingsLocale,
            deviceLocale = deviceLocale,
        )
    }

    var loading by remember(trimmed) { mutableStateOf(false) }
    var translated by remember(trimmed) { mutableStateOf<String?>(null) }
    var sourceLang by remember(trimmed) { mutableStateOf<String?>(null) }
    var error by remember(trimmed) { mutableStateOf<String?>(null) }
    var showingTranslation by remember(trimmed) { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
    ) {
        if (showingTranslation && translated != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(AppTheme.extraColorScheme.surfaceVariantAlt1)
                    .border(1.dp, AppTheme.extraColorScheme.surfaceVariantAlt2, shape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                text = translated.orEmpty(),
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyMedium,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val detected = sourceLang
                if (!detected.isNullOrBlank()) {
                    Text(
                        text = stringResource(
                            id = R.string.feed_note_translate_translated_from,
                            NoteTranslate.languageDisplayName(detected, deviceLocale),
                        ),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        style = AppTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TranslateActionText(
                    text = stringResource(id = R.string.feed_note_translate_show_original),
                    enabled = true,
                    onClick = { showingTranslation = false },
                )
            }
        } else {
            TranslateActionText(
                text = if (loading) {
                    stringResource(id = R.string.feed_note_translate_translating)
                } else {
                    stringResource(id = R.string.feed_note_translate_action)
                },
                enabled = !loading,
                onClick = {
                    if (loading) return@TranslateActionText
                    if (translated != null) {
                        showingTranslation = true
                        return@TranslateActionText
                    }
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            val result = NoteTranslate.translateText(trimmed, targetLang)
                            translated = result.translatedText
                            sourceLang = result.detectedSourceLanguage
                            showingTranslation = true
                        } catch (ex: Exception) {
                            error = ex.message ?: ex.toString()
                        } finally {
                            loading = false
                        }
                    }
                },
            )
        }

        val errorMessage = error
        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.feed_note_translate_failed, errorMessage),
                color = AppTheme.colorScheme.error,
                style = AppTheme.typography.bodySmall,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun TranslateActionText(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier.clickable(
            enabled = enabled,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick,
        ),
        text = text,
        color = AppTheme.colorScheme.secondary.copy(alpha = if (enabled) 1f else 0.7f),
        style = AppTheme.typography.bodyMedium,
        fontSize = 14.sp,
    )
}
