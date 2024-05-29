package net.primal.android.editor.domain

import android.net.Uri
import java.util.*

data class NoteAttachment(
    val id: UUID = UUID.randomUUID(),
    val localUri: Uri,
    val remoteUrl: String? = null,
    val mimeType: String? = null,
    val originalHash: String? = null,
    val uploadedHash: String? = null,
    val sizeInBytes: Int? = null,
    val dimensionInPixels: String? = null,
    val uploadError: Throwable? = null,
) {
    val isImageAttachment: Boolean get() = mimeType?.startsWith("image") == true
}
