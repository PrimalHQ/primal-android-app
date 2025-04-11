package net.primal.android.networking.upload

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.core.networking.primal.api.BlossomUploader
import net.primal.core.networking.primal.api.UnsuccessfulFileUpload
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.upload.UploadResult
import okio.buffer
import okio.source

class FileUploadRepository @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val contentResolver: ContentResolver,
    private val blossomUploader: BlossomUploader,
) {

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun upload(
        uri: Uri,
        userId: String,
        uploadId: String,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        withContext(dispatchers.io()) {
            val descriptor = blossomUploader.uploadBlob(
                userId = userId,
                blossomUrl = "https://blossom.primal.net",
                inputStream = {
                    contentResolver.openInputStream(uri)?.source()?.buffer()
                        ?: throw IOException("Unable to open input stream.")
                },
                onProgress = onProgress,
            )

            UploadResult(
                remoteUrl = descriptor.url,
                originalFileSize = descriptor.sizeInBytes,
                originalHash = descriptor.sha256,
            )
        }

    suspend fun cancelNoteAttachmentUpload(userId: String, uploadId: String) {
        // TODO Remove this and clean the code
    }
}
