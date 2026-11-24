package net.primal.android.notes.feed.note.translation

internal sealed interface NoteTranslationUiState {
    data object Idle : NoteTranslationUiState
    data object Loading : NoteTranslationUiState
    data class Success(val translation: TranslatedNote) : NoteTranslationUiState
    data class Error(val reason: NoteTranslationException) : NoteTranslationUiState
}
