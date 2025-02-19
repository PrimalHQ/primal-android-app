package net.primal.android.attachments.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.primal.upload.PrimalFileUploader
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.primal.upload.domain.UploadResult
import net.primal.android.user.accounts.active.ActiveAccountStore

class AttachmentsRepository @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val fileUploader: PrimalFileUploader,
    private val database: PrimalDatabase,
    private val dispatchers: CoroutineDispatcherProvider,
) {

    fun loadAttachments(noteId: String, types: List<NoteAttachmentType>): List<NoteAttachment> {
        return database.attachments().loadNoteAttachments(noteId = noteId, types = types)
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadNoteAttachment(
        attachment: net.primal.android.editor.domain.NoteAttachment,
        uploadId: String,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult = withContext(dispatchers.io()) {
        val userId = activeAccountStore.activeUserId()
        fileUploader.uploadFile(
            uri = attachment.localUri,
            userId = userId,
            uploadId = uploadId,
            onProgress = onProgress,
        )
    }

    suspend fun cancelNoteAttachmentUpload(uploadId: String) {
        val userId = activeAccountStore.activeUserId()
        fileUploader.cancelUpload(userId = userId, uploadId = uploadId)
    }
}
