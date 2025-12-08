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
        "SELECT * FROM AppSessionData WHERE clientPubKey = :clientPubKey AND endedAt IS NULL AND activeRelayCount > 0",
    )
    suspend fun findActiveSessionByClientPubKey(clientPubKey: String): AppSession?

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    suspend fun findSession(sessionId: String): AppSession?

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData s 
        JOIN AppConnectionData c ON s.clientPubKey = c.clientPubKey
        WHERE activeRelayCount > 0 AND endedAt IS NULL AND c.signerPubKey = :signerPubKey
    """,
    )
    fun observeActiveSessions(signerPubKey: Encryptable<String>): Flow<List<AppSession>>

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData s 
        JOIN AppConnectionData c ON s.clientPubKey = c.clientPubKey
        WHERE endedAt IS NULL AND c.signerPubKey = :signerPubKey
    """,
    )
    fun observeOngoingSessions(signerPubKey: Encryptable<String>): Flow<List<AppSession>>

    @Transaction
    @Query(
        "SELECT * FROM AppSessionData WHERE clientPubKey = :clientPubKey AND endedAt IS NULL AND activeRelayCount > 0",
    )
    fun observeActiveSessionForConnection(clientPubKey: String): Flow<AppSession?>

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE clientPubKey = :clientPubKey ORDER BY startedAt DESC")
    fun observeSessionsByClientPubKey(clientPubKey: String): Flow<List<AppSession>>

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

    @Query("DELETE FROM AppSessionData WHERE clientPubKey = :clientPubKey")
    suspend fun deleteSessions(clientPubKey: String)
}
