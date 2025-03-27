package net.primal.android.editor.domain

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Serializable
import net.primal.android.nostr.utils.Nevent
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString

@Serializable
data class NoteEditorArgs(
    val referencedNoteNevent: String? = null,
    val referencedArticleNaddr: String? = null,
    val referencedHighlightNevent: String? = null,
    val mediaUris: List<String> = emptyList(),
    val content: String = "",
    val contentSelectionStart: Int = 0,
    val contentSelectionEnd: Int = 0,
    val taggedUsers: List<NoteTaggedUser> = emptyList(),
    val isQuoting: Boolean = false,
) {
    fun toJson(): String = this.encodeToJsonString()

    companion object {
        fun String.jsonAsNoteEditorArgs(): NoteEditorArgs? {
            return this.decodeFromJsonStringOrNull<NoteEditorArgs>()
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
