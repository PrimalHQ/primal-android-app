package net.primal.android.notes.translation

import androidx.compose.runtime.Immutable

@Immutable
data class NoteTranslation(
    val text: String,
    val detectedLanguage: String,
    val targetLanguage: String,
)
