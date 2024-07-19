package net.primal.android.note.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: EventStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<EventStats>)

    @Query("SELECT * FROM EventStats WHERE eventId = :eventId")
    fun find(eventId: String): EventStats?
}
