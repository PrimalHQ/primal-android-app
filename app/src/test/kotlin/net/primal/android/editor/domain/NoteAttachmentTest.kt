package net.primal.android.editor.domain

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldContain
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteAttachmentTest {
    private fun createNoteAttachment(
        uri: Uri = Uri.EMPTY,
        remoteUrl: String? = "https://uploads.primal.net/image.jpg",
        mimeType: String? = null,
        originalHash: String? = null,
        uploadedHash: String? = null,
        sizeInBytes: Int? = null,
        dimensionInPixels: String? = null,
        uploadError: Throwable? = null,
    ): NoteAttachment {
        return NoteAttachment(
            localUri = uri,
            remoteUrl = remoteUrl,
            mimeType = mimeType,
            originalHash = originalHash,
            uploadedHash = uploadedHash,
            uploadedSizeInBytes = sizeInBytes,
            dimensionInPixels = dimensionInPixels,
            uploadError = uploadError,
        )
    }

    @Test
    fun createsIMetaTag_withMimeTypeIfAvailable() {
        createNoteAttachment(mimeType = "image/jpeg").asIMetaTag()
            .shouldContain(JsonPrimitive("m image/jpeg"))
    }

    @Test
    fun createsIMetaTag_withDimensionsIfAvailable() {
        createNoteAttachment(dimensionInPixels = "100x200").asIMetaTag()
            .shouldContain(JsonPrimitive("dim 100x200"))
    }

    @Test
    fun createsIMetaTag_withSizeInBytesIfAvailable() {
        createNoteAttachment(sizeInBytes = 6425281).asIMetaTag()
            .shouldContain(JsonPrimitive("size 6425281"))
    }

    @Test
    fun createsIMetaTag_withOriginalHashIfAvailable() {
        createNoteAttachment(originalHash = "original").asIMetaTag()
            .shouldContain(JsonPrimitive("ox original"))
    }

    @Test
    fun createsIMetaTag_withUploadedHashIfAvailable() {
        createNoteAttachment(uploadedHash = "uploaded").asIMetaTag()
            .shouldContain(JsonPrimitive("x uploaded"))
    }
}
