package net.primal.data.account.local.dao.apps.remote

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteAppSessionDataDao {
    @Upsert
    suspend fun upsertAll(data: List<RemoteAppSessionData>)

    @Transaction
    @Query(
        """
        SELECT * FROM RemoteAppSessionData 
        WHERE clientPubKey = :clientPubKey AND endedAt IS NULL AND activeRelayCount > 0
        """,
    )
    suspend fun findActiveSessionByClientPubKey(clientPubKey: String): RemoteAppSession?

    @Transaction
    @Query("SELECT * FROM RemoteAppSessionData WHERE sessionId = :sessionId")
    suspend fun findSession(sessionId: String): RemoteAppSession?

    @Transaction
    @Query(
        """
        SELECT * FROM RemoteAppSessionData s 
        JOIN RemoteAppConnectionData c ON s.clientPubKey = c.clientPubKey
        WHERE activeRelayCount > 0 AND endedAt IS NULL AND c.signerPubKey = :signerPubKey
    """,
    )
    fun observeActiveSessions(signerPubKey: String): Flow<List<RemoteAppSession>>

    @Transaction
    @Query(
        """
        SELECT * FROM RemoteAppSessionData s 
        JOIN RemoteAppConnectionData c ON s.clientPubKey = c.clientPubKey
        WHERE endedAt IS NULL AND c.signerPubKey = :signerPubKey
        """,
    )
    fun observeOngoingSessions(signerPubKey: String): Flow<List<RemoteAppSession>>

    @Transaction
    @Query(
        """
            SELECT * FROM RemoteAppSessionData
        WHERE clientPubKey = :clientPubKey AND endedAt IS NULL AND activeRelayCount > 0""",
    )
    fun observeActiveSessionForConnection(clientPubKey: String): Flow<RemoteAppSession?>

    @Transaction
    @Query("SELECT * FROM RemoteAppSessionData WHERE clientPubKey = :clientPubKey ORDER BY startedAt DESC")
    fun observeSessionsByClientPubKey(clientPubKey: String): Flow<List<RemoteAppSession>>

    @Transaction
    @Query("SELECT * FROM RemoteAppSessionData WHERE sessionId = :sessionId")
    fun observeSession(sessionId: String): Flow<RemoteAppSession?>

    @Query("UPDATE RemoteAppSessionData SET endedAt = :endedAt, activeRelayCount = 0 WHERE sessionId = :sessionId")
    suspend fun endSession(sessionId: String, endedAt: Long)

    @Query(
        """
        UPDATE RemoteAppSessionData
        SET endedAt = :endedAt, activeRelayCount = 0
        WHERE endedAt IS NULL
    """,
    )
    suspend fun endAllActiveSessions(endedAt: Long)

    @Query("UPDATE RemoteAppSessionData SET activeRelayCount = activeRelayCount + 1 WHERE sessionId = :sessionId")
    suspend fun incrementActiveRelayCount(sessionId: String)

    @Query(
        """
        UPDATE RemoteAppSessionData 
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

    @Query("UPDATE RemoteAppSessionData SET activeRelayCount = :activeRelayCount WHERE sessionId = :sessionId")
    suspend fun setActiveRelayCount(sessionId: String, activeRelayCount: Int)

    @Query("DELETE FROM RemoteAppSessionData WHERE clientPubKey = :clientPubKey")
    suspend fun deleteSessions(clientPubKey: String)
}
