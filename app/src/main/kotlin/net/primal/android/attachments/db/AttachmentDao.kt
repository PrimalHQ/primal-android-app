package net.primal.android.attachments.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllNoteAttachments(data: List<NoteAttachment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllNostrUris(data: List<NoteNostrUri>)
}
