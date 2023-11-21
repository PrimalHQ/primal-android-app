package net.primal.android.attachments.repository

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.db.PrimalDatabase

class AttachmentsRepository @Inject constructor(
    private val database: PrimalDatabase,
) {

    suspend fun loadAttachments(noteId: String, types: List<NoteAttachmentType>): List<NoteAttachment> {
        return withContext(Dispatchers.IO) {
            database.attachments().loadNoteAttachments(noteId = noteId, types = types)
        }
    }
}
