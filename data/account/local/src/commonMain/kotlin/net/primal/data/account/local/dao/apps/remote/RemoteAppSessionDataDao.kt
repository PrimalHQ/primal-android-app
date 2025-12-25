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
        WHERE appIdentifier = :appIdentifier AND endedAt IS NULL
        ORDER BY startedAt DESC
        """,
    )
    suspend fun findOngoingSessionByApp(appIdentifier: String): RemoteAppSession?

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData
        WHERE appIdentifier = :appIdentifier AND endedAt IS NULL
        ORDER BY startedAt DESC
        """,
    )
    fun observeOngoingSessionByApp(appIdentifier: String): Flow<RemoteAppSession?>

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    suspend fun findSession(sessionId: String): RemoteAppSession?

    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    fun observeSession(sessionId: String): Flow<RemoteAppSession?>

    @Transaction
    @Query(
        """
        SELECT * FROM AppSessionData s 
        JOIN RemoteAppConnectionData c ON s.appIdentifier = c.clientPubKey
        WHERE endedAt IS NULL AND c.signerPubKey = :signerPubKey AND s.sessionType = 'RemoteSession'
        """,
    )
    fun observeOngoingSessionsBySigner(signerPubKey: String): Flow<List<RemoteAppSession>>
}
