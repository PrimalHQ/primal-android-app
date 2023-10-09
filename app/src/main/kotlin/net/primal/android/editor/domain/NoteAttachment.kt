package net.primal.android.editor.domain

import android.net.Uri
import java.util.UUID

data class NoteAttachment(
    val id: UUID = UUID.randomUUID(),
    val localUri: Uri,
    val mimeType: String? = null,
    val otherRelevantInfo: String? = null,
    val remoteUrl: String? = null,
    val uploadError: Throwable? = null,
) {
    val isImageAttachment: Boolean get() = mimeType?.startsWith("image") == true
}
