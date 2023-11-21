package net.primal.android.attachments.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.primal.android.attachments.domain.NoteAttachmentType

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllNostrUris(data: List<NoteNostrUri>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllNoteAttachments(data: List<NoteAttachment>)

    @Query("SELECT * FROM NoteAttachment WHERE eventId = :noteId AND type IN (:types)")
    fun loadNoteAttachments(
        noteId: String,
        types: List<NoteAttachmentType> = NoteAttachmentType.entries,
    ): List<NoteAttachment>
}
