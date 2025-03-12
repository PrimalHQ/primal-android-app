package net.primal.android.events.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventUserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: EventUserStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<EventUserStats>)

    @Query("SELECT * FROM EventUserStats WHERE eventId = :eventId AND userId = :userId")
    fun find(eventId: String, userId: String): EventUserStats?

    @Query("SELECT * FROM EventUserStats WHERE eventId IN (:eventIds) AND userId = :userId")
    fun observeStats(eventIds: List<String>, userId: String): Flow<List<EventUserStats>>
}
