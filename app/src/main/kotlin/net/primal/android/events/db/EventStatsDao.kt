package net.primal.android.events.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: EventStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<EventStats>)

    @Query("SELECT * FROM EventStats WHERE eventId = :eventId")
    fun find(eventId: String): EventStats?

    @Query("SELECT * FROM EventStats WHERE eventId IN (:eventIds)")
    fun observeStats(eventIds: List<String>): Flow<List<EventStats>>
}
