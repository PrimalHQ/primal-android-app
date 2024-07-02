package net.primal.android.attachments.repository

import java.util.*
import javax.inject.Inject
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.primal.upload.PrimalFileUploader
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.primal.upload.domain.UploadResult
import net.primal.android.user.accounts.active.ActiveAccountStore

class AttachmentsRepository @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val fileUploader: PrimalFileUploader,
    private val database: PrimalDatabase,
) {

    fun loadAttachments(noteId: String, types: List<NoteAttachmentType>): List<NoteAttachment> {
        return database.attachments().loadNoteAttachments(noteId = noteId, types = types)
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadNoteAttachment(
        attachment: net.primal.android.editor.domain.NoteAttachment,
        uploadId: UUID,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        val userId = activeAccountStore.activeUserId()
        return fileUploader.uploadFile(
            uri = attachment.localUri,
            userId = userId,
            uploadId = uploadId,
            onProgress = onProgress,
        )
    }

    suspend fun cancelNoteAttachmentUpload(uploadId: UUID) {
        val userId = activeAccountStore.activeUserId()
        fileUploader.cancelUpload(userId = userId, uploadId = uploadId)
    }
}
