package net.primal.android.note.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventUserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: EventUserStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<EventUserStats>)

    @Query("SELECT * FROM EventUserStats WHERE eventId = :eventId AND userId = :userId")
    fun find(eventId: String, userId: String): EventUserStats?
}
