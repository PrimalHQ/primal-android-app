package net.primal.android.events.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.events.db.EventUri
import net.primal.android.events.domain.EventUriType
import net.primal.android.networking.primal.upload.PrimalFileUploader
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.domain.upload.UploadResult

class EventUriRepository @Inject constructor(
    private val fileUploader: PrimalFileUploader,
    private val database: PrimalDatabase,
    private val dispatchers: CoroutineDispatcherProvider,
) {

    fun loadEventUris(noteId: String, types: List<EventUriType>): List<EventUri> {
        return database.eventUris().loadEventUris(noteId = noteId, types = types)
    }

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
