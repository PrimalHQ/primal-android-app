package net.primal.android.notes.translation

import java.util.Locale
import net.primal.android.user.domain.NoteTranslationSettings

data class NoteTranslationLanguage(
    val code: String,
    val englishName: String,
)

object NoteTranslationLanguages {

    val supported: List<NoteTranslationLanguage> = listOf(
        NoteTranslationLanguage("en", "English"),
        NoteTranslationLanguage("es", "Spanish"),
        NoteTranslationLanguage("pt", "Portuguese"),
        NoteTranslationLanguage("fr", "French"),
        NoteTranslationLanguage("de", "German"),
        NoteTranslationLanguage("it", "Italian"),
        NoteTranslationLanguage("nl", "Dutch"),
        NoteTranslationLanguage("pl", "Polish"),
        NoteTranslationLanguage("ru", "Russian"),
        NoteTranslationLanguage("uk", "Ukrainian"),
        NoteTranslationLanguage("ja", "Japanese"),
        NoteTranslationLanguage("ko", "Korean"),
        NoteTranslationLanguage("zh", "Chinese (Simplified)"),
        NoteTranslationLanguage("zh-TW", "Chinese (Traditional)"),
        NoteTranslationLanguage("ar", "Arabic"),
        NoteTranslationLanguage("he", "Hebrew"),
        NoteTranslationLanguage("hi", "Hindi"),
        NoteTranslationLanguage("tr", "Turkish"),
        NoteTranslationLanguage("sv", "Swedish"),
        NoteTranslationLanguage("nb", "Norwegian"),
        NoteTranslationLanguage("da", "Danish"),
        NoteTranslationLanguage("fi", "Finnish"),
        NoteTranslationLanguage("cs", "Czech"),
        NoteTranslationLanguage("ro", "Romanian"),
        NoteTranslationLanguage("hu", "Hungarian"),
        NoteTranslationLanguage("el", "Greek"),
        NoteTranslationLanguage("id", "Indonesian"),
        NoteTranslationLanguage("vi", "Vietnamese"),
        NoteTranslationLanguage("th", "Thai"),
    )

    fun resolveTargetLanguageCode(stored: String): String {
        val trimmed = stored.trim()
        if (trimmed.isEmpty() || trimmed == NoteTranslationSettings.DEVICE_LANGUAGE_CODE) {
            return deviceLanguageCode()
        }
        return trimmed
    }

    fun deviceLanguageCode(): String {
        val language = Locale.getDefault().language
        return language.ifBlank { "en" }
    }

    fun displayName(code: String): String {
        if (code.isBlank()) return "Device language"
        return supported.firstOrNull { it.code.equals(code, ignoreCase = true) }?.englishName
            ?: Locale.forLanguageTag(code).displayLanguage.ifBlank { code }
    }
}
