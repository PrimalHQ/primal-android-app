package net.primal.data.account.local.dao.apps.remote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface RemoteAppSessionEventDataDao {
    @Insert
    suspend fun insert(data: RemoteAppSessionEventData)

    @Query(
        """
        SELECT * FROM RemoteAppSessionEventData
        WHERE sessionId = :sessionId AND (requestState = 'Approved' OR requestState = 'Rejected')
        ORDER BY requestedAt DESC
    """,
    )
    fun observeCompletedEventsBySessionId(sessionId: String): Flow<List<RemoteAppSessionEventData>>

    @Query("SELECT * FROM RemoteAppSessionEventData WHERE eventId = :eventId")
    fun observeEvent(eventId: String): Flow<RemoteAppSessionEventData?>

    @Query(
        "SELECT * FROM RemoteAppSessionEventData WHERE signerPubKey = :signerPubKey AND requestState = :requestState",
    )
    fun observeEventsByRequestState(
        signerPubKey: String,
        requestState: RemoteAppRequestState,
    ): Flow<List<RemoteAppSessionEventData>>

    @Query(
        """
        UPDATE RemoteAppSessionEventData
        SET requestState = :requestState, responsePayload = :responsePayload, completedAt = :completedAt
        WHERE eventId = :eventId
        """,
    )
    suspend fun updateSessionEventRequestState(
        eventId: String,
        requestState: RemoteAppRequestState,
        responsePayload: Encryptable<String>?,
        completedAt: Long?,
    )

    @Query("SELECT * FROM RemoteAppSessionEventData WHERE eventId = :eventId")
    suspend fun getSessionEvent(eventId: String): RemoteAppSessionEventData?

    @Query("DELETE FROM RemoteAppSessionEventData WHERE clientPubKey = :clientPubKey")
    suspend fun deleteEvents(clientPubKey: String)
}
