package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionEventDataDao {
    @Upsert
    suspend fun upsert(data: SessionEventData)

    @Query("SELECT * FROM SessionEventData WHERE sessionId = :sessionId ORDER BY requestedAt DESC")
    fun observeEventsBySessionId(sessionId: String): Flow<List<SessionEventData>>
}
