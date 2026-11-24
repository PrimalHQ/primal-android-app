package net.primal.android.notes.translation

sealed class NoteTranslationOutcome {
    data class Translated(
        val text: String,
        val source: NoteTranslationSource,
    ) : NoteTranslationOutcome()

    data class AlreadyInTarget(val text: String) : NoteTranslationOutcome()

    data object NotConfigured : NoteTranslationOutcome()

    data class Failed(val cause: Throwable? = null) : NoteTranslationOutcome()
}

enum class NoteTranslationSource {
    OnDevice,
    LibreTranslate,
    DeepL,
    Google,
}

fun NetworkTranslationRoute.toSource(): NoteTranslationSource =
    when (this) {
        is NetworkTranslationRoute.LibreTranslate -> NoteTranslationSource.LibreTranslate
        is NetworkTranslationRoute.DeepL -> NoteTranslationSource.DeepL
        is NetworkTranslationRoute.Google -> NoteTranslationSource.Google
    }
