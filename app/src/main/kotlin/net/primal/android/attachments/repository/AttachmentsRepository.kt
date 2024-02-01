package net.primal.android.attachments.repository

import javax.inject.Inject
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.db.PrimalDatabase

class AttachmentsRepository @Inject constructor(
    private val database: PrimalDatabase,
) {
    fun loadAttachments(noteId: String, types: List<NoteAttachmentType>): List<NoteAttachment> {
        return database.attachments().loadNoteAttachments(noteId = noteId, types = types)
    }
}
