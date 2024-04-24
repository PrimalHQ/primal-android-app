package net.primal.android.note.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: NoteStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<NoteStats>)

    @Query("SELECT * FROM NoteStats WHERE postId = :postId")
    fun find(postId: String): NoteStats?
}
