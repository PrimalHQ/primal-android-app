package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface AppSessionDataDao {
    @Upsert
    suspend fun upsertAll(data: List<AppSessionData>)

    @Transaction
    @Query(
        "SELECT * FROM AppSessionData WHERE connectionId = :connectionId AND endedAt IS NULL AND activeRelayCount > 0",
    )
    suspend fun findActiveSessionByConnectionId(connectionId: String): AppSession?

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    suspend fun findSession(sessionId: String): AppSession?

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData s 
        JOIN AppConnectionData c ON s.connectionId = c.connectionId
        WHERE activeRelayCount > 0 AND c.signerPubKey = :signerPubKey
    """,
    )
    fun observeActiveSessions(signerPubKey: Encryptable<String>): Flow<List<AppSession>>

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData s 
        JOIN AppConnectionData c ON s.connectionId = c.connectionId
        WHERE endedAt IS NULL AND c.signerPubKey = :signerPubKey
    """,
    )
    fun observeOngoingSessions(signerPubKey: Encryptable<String>): Flow<List<AppSession>>

    @Transaction
    @Query(
        "SELECT * FROM AppSessionData WHERE connectionId = :connectionId AND endedAt IS NULL AND activeRelayCount > 0",
    )
    fun observeActiveSessionForConnection(connectionId: String): Flow<AppSession?>

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE connectionId = :connectionId ORDER BY startedAt DESC")
    fun observeSessionsByConnectionId(connectionId: String): Flow<List<AppSession>>

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    fun observeSession(sessionId: String): Flow<AppSession?>

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

    @Query("DELETE FROM AppSessionData WHERE connectionId = :connectionId")
    suspend fun deleteSessionsByConnectionId(connectionId: String)
}
