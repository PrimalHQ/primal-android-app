package net.primal.android.note.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteUserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: NoteUserStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<NoteUserStats>)

    @Query("SELECT * FROM NoteUserStats WHERE postId = :postId AND userId = :userId")
    fun find(postId: String, userId: String): NoteUserStats?
}
