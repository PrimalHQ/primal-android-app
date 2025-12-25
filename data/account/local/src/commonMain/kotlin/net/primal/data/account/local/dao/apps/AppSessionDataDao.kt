package net.primal.data.account.local.dao.apps

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSessionDataDao {
    @Upsert
    suspend fun upsertAll(data: List<AppSessionData>)

    @Query(
        """
            SELECT * FROM AppSessionData
            WHERE appIdentifier = :appIdentifier
            ORDER BY startedAt DESC
        """,
    )
    suspend fun findLatestSessionByApp(appIdentifier: String): AppSessionData?

    @Query("UPDATE AppSessionData SET endedAt = :endedAt WHERE sessionId = :sessionId")
    suspend fun endSession(sessionId: String, endedAt: Long)

    @Query(
        """
        UPDATE AppSessionData
        SET endedAt = :endedAt
        WHERE endedAt IS NULL
    """,
    )
    suspend fun endAllOngoingSessions(endedAt: Long)

    @Query("DELETE FROM AppSessionData WHERE appIdentifier = :appIdentifier")
    suspend fun deleteAllSessionsByApp(appIdentifier: String)

    @Query("SELECT * FROM AppSessionData WHERE appIdentifier = :appIdentifier ORDER BY startedAt DESC")
    fun observeSessionsByApp(appIdentifier: String): Flow<List<AppSessionData>>

    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    fun observeSession(sessionId: String): Flow<AppSessionData?>
}
