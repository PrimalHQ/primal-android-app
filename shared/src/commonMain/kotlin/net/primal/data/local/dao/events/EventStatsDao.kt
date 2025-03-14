package net.primal.data.local.dao.events

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: EventStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<EventStats>)

    @Query("SELECT * FROM EventStats WHERE eventId = :eventId")
    suspend fun find(eventId: String): EventStats?

    @Query("SELECT * FROM EventStats WHERE eventId IN (:eventIds)")
    fun observeStats(eventIds: List<String>): Flow<List<EventStats>>
}
