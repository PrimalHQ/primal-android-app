package net.primal.android.editor.domain

import android.net.Uri
import java.util.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

data class NoteAttachment(
    val id: UUID = UUID.randomUUID(),
    val localUri: Uri,
    val remoteUrl: String? = null,
    val mimeType: String? = null,
    val originalHash: String? = null,
    val uploadedHash: String? = null,
    val originalUploadedInBytes: Int? = null,
    val originalSizeInBytes: Int? = null,
    val uploadedSizeInBytes: Int? = null,
    val dimensionInPixels: String? = null,
    val uploadError: Throwable? = null,
) {
    val isImageAttachment: Boolean get() = mimeType?.startsWith("image") == true
}

fun NoteAttachment.asIMetaTag(): JsonArray {
    require(this.remoteUrl != null)
    return buildJsonArray {
        add("imeta")
        add("url ${this@asIMetaTag.remoteUrl}")
        this@asIMetaTag.mimeType?.let { add("m $it") }
        this@asIMetaTag.uploadedHash?.let { add("x $it") }
        this@asIMetaTag.originalHash?.let { add("ox $it") }
        this@asIMetaTag.uploadedSizeInBytes?.let { add("size $it") }
        this@asIMetaTag.dimensionInPixels?.let { add("dim $it") }
    }
}
