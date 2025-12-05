package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface SessionEventDataDao {
    @Insert
    suspend fun insert(data: SessionEventData)

    @Query(
        """
        SELECT * FROM SessionEventData
        WHERE sessionId = :sessionId AND (requestState = 'Approved' OR requestState = 'Rejected')
        ORDER BY requestedAt DESC
    """,
    )
    fun observeCompletedEventsBySessionId(sessionId: String): Flow<List<SessionEventData>>

    @Query("SELECT * FROM SessionEventData WHERE eventId = :eventId")
    fun observeEvent(eventId: String): Flow<SessionEventData?>

    @Query("SELECT * FROM SessionEventData WHERE signerPubKey = :signerPubKey AND requestState = :requestState")
    fun observeEventsByRequestState(
        signerPubKey: Encryptable<String>,
        requestState: RequestState,
    ): Flow<List<SessionEventData>>

    @Query(
        """
        UPDATE SessionEventData
        SET requestState = :requestState, responsePayload = :responsePayload, completedAt = :completedAt
        WHERE eventId = :eventId
        """,
    )
    suspend fun updateSessionEventRequestState(
        eventId: String,
        requestState: RequestState,
        responsePayload: Encryptable<String>?,
        completedAt: Long?,
    )

    @Query("SELECT * FROM SessionEventData WHERE eventId = :eventId")
    suspend fun getSessionEvent(eventId: String): SessionEventData?

    @Query(
        """
        DELETE FROM SessionEventData 
        WHERE sessionId IN (SELECT sessionId FROM AppSessionData WHERE clientPubKey = :clientPubKey)""",
    )
    suspend fun deleteEvents(clientPubKey: String)
}
