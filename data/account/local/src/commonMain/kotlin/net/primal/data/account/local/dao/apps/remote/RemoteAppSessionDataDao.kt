package net.primal.data.account.local.dao.apps.remote

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteAppSessionDataDao {

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData 
        WHERE appIdentifier = :appIdentifier AND endedAt IS NULL AND activeRelayCount > 0
        """,
    )
    suspend fun findActiveSessionByClientPubKey(appIdentifier: String): RemoteAppSession?

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData 
        WHERE appIdentifier = :appIdentifier AND endedAt IS NULL
        """,
    )
    suspend fun findAnyOpenSessionByAppIdentifier(appIdentifier: String): RemoteAppSession?

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    suspend fun findSession(sessionId: String): RemoteAppSession?

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData s 
        JOIN RemoteAppConnectionData c ON s.appIdentifier = c.clientPubKey
        WHERE activeRelayCount > 0 AND endedAt IS NULL AND c.signerPubKey = :signerPubKey
    """,
    )
    fun observeActiveSessions(signerPubKey: String): Flow<List<RemoteAppSession>>

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData s 
        JOIN RemoteAppConnectionData c ON s.appIdentifier = c.clientPubKey
        WHERE endedAt IS NULL AND c.signerPubKey = :signerPubKey
        """,
    )
    fun observeOngoingSessions(signerPubKey: String): Flow<List<RemoteAppSession>>

    @Transaction
    @Query(
        """
            SELECT * FROM AppSessionData
        WHERE appIdentifier = :appIdentifier AND endedAt IS NULL AND activeRelayCount > 0""",
    )
    fun observeActiveSessionForConnection(appIdentifier: String): Flow<RemoteAppSession?>

    @Transaction
    @Query(
        """
            SELECT * FROM AppSessionData
        WHERE appIdentifier = :appIdentifier AND endedAt IS NULL
        """,
    )
    fun observeOngoingSessionForConnection(appIdentifier: String): Flow<RemoteAppSession?>

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE appIdentifier = :appIdentifier ORDER BY startedAt DESC")
    fun observeSessionsByClientPubKey(appIdentifier: String): Flow<List<RemoteAppSession>>

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    fun observeSession(sessionId: String): Flow<RemoteAppSession?>
}
