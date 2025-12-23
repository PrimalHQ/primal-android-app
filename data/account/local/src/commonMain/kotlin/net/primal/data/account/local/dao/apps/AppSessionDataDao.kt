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
            LIMIT 1
        """,
    )
    suspend fun findLatestAppSession(appIdentifier: String): AppSessionData?

    @Query("UPDATE AppSessionData SET endedAt = :endedAt, activeRelayCount = 0 WHERE sessionId = :sessionId")
    suspend fun endSession(sessionId: String, endedAt: Long)

    @Query(
        """
        UPDATE AppSessionData
        SET endedAt = :endedAt, activeRelayCount = 0
        WHERE endedAt IS NULL
    """,
    )
    suspend fun endAllActiveSessions(endedAt: Long)

    @Query("UPDATE AppSessionData SET activeRelayCount = activeRelayCount + 1 WHERE sessionId = :sessionId")
    suspend fun incrementActiveRelayCount(sessionId: String)

    @Query(
        """
        UPDATE AppSessionData 
        SET activeRelayCount = activeRelayCount - 1,
            endedAt = CASE
                WHEN activeRelayCount - 1 <= 0 
                    THEN strftime('%s', 'now')
                    ELSE endedAt
            END
        WHERE sessionId = :sessionId
    """,
    )
    suspend fun decrementActiveRelayCountOrEnd(sessionId: String)

    @Query("UPDATE AppSessionData SET activeRelayCount = :activeRelayCount WHERE sessionId = :sessionId")
    suspend fun setActiveRelayCount(sessionId: String, activeRelayCount: Int)

    @Query("DELETE FROM AppSessionData WHERE appIdentifier = :appIdentifier")
    suspend fun deleteSessions(appIdentifier: String)

    @Query("SELECT * FROM AppSessionData WHERE appIdentifier = :appIdentifier ORDER BY startedAt DESC")
    fun observeSessionsByAppIdentifier(appIdentifier: String): Flow<List<AppSessionData>>
}
