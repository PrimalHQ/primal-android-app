package net.primal.android.notes.translation.device

fun interface OnDeviceNoteTranslator {
    suspend fun translate(text: String, targetLanguage: String): String?
}
