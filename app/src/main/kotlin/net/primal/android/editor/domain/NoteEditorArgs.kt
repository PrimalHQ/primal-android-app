package net.primal.android.editor.domain

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull

@Serializable
data class NoteEditorArgs(
    val replyToNoteId: String? = null,
    val replyToArticleNaddr: String? = null,
    val mediaUris: List<String> = emptyList(),
    val content: String = "",
    val contentSelectionStart: Int = 0,
    val contentSelectionEnd: Int = 0,
    val taggedUsers: List<NoteTaggedUser> = emptyList(),
) {
    fun toJson(): String = NostrJson.encodeToString(this)

    companion object {
        fun String.asNoteEditorArgs(): NoteEditorArgs? {
            return NostrJson.decodeFromStringOrNull<NoteEditorArgs>(this)
        }

        fun TextFieldValue.asNoteEditorArgs(): NoteEditorArgs {
            return NoteEditorArgs(
                content = this.text,
                contentSelectionStart = this.selection.start,
                contentSelectionEnd = this.selection.end,
            )
        }
    }
}
