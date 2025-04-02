package net.primal.android.networking.primal.upload.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.networking.primal.upload.PrimalFileUploader
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.upload.UploadResult

class FileUploadRepository @Inject constructor(
    private val fileUploader: PrimalFileUploader,
    private val dispatchers: DispatcherProvider,
) {

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadNoteAttachment(
        userId: String,
        attachment: NoteAttachment,
        uploadId: String,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        withContext(dispatchers.io()) {
            fileUploader.uploadFile(
                uri = attachment.localUri,
                userId = userId,
                uploadId = uploadId,
                onProgress = onProgress,
            )
        }

    suspend fun cancelNoteAttachmentUpload(userId: String, uploadId: String) {
        fileUploader.cancelUpload(userId = userId, uploadId = uploadId)
    }
}
