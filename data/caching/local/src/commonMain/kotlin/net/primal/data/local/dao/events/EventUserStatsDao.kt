package net.primal.data.local.dao.events

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.data.local.db.chunkedFlowQuery

@Dao
interface EventUserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: EventUserStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<EventUserStats>)

    @Query("SELECT * FROM EventUserStats WHERE eventId = :eventId AND userId = :userId")
    suspend fun find(eventId: String, userId: String): EventUserStats?

    @Query("DELETE FROM EventUserStats WHERE eventId = :eventId")
    suspend fun deleteByEventId(eventId: String)

    @Transaction
    suspend fun reduceEventUserStats(
        eventId: String,
        userId: String,
        reducer: EventUserStats.() -> EventUserStats,
    ) {
        find(eventId = eventId, userId = userId)?.let {
            upsert(it.reducer())
        }
    }

    @Query("SELECT * FROM EventUserStats WHERE eventId IN (:eventIds) AND userId = :userId")
    @Suppress("ktlint:standard:function-naming")
    fun _observeStats(eventIds: List<String>, userId: String): Flow<List<EventUserStats>>

    fun observeStats(eventIds: List<String>, userId: String): Flow<List<EventUserStats>> =
        eventIds.chunkedFlowQuery { _observeStats(eventIds = it, userId = userId) }
}
