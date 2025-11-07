package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionLogDataDao {
    @Upsert
    suspend fun upsert(data: SessionLogData)

    @Transaction
    @Query("SELECT * FROM SessionLogData WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    fun observeLogsBySessionId(sessionId: String): Flow<List<SessionLogData>>
}
