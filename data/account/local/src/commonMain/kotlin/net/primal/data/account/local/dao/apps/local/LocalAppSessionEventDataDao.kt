package net.primal.data.account.local.dao.apps.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface LocalAppSessionEventDataDao {
    @Insert
    suspend fun insert(data: LocalAppSessionEventData)

    @Query(
        """
        SELECT * FROM LocalAppSessionEventData
        WHERE sessionId = :sessionId AND (requestState = 'Approved' OR requestState = 'Rejected')
        ORDER BY requestedAt DESC
        """,
    )
    fun observeCompletedEventsBySessionId(sessionId: String): Flow<List<LocalAppSessionEventData>>

    @Query(
        """
        SELECT * FROM LocalAppSessionEventData 
        WHERE appIdentifier = :appIdentifier AND requestState = :requestState
        """,
    )
    fun observeEventsByAppIdentifierAndRequestState(
        appIdentifier: String,
        requestState: AppRequestState,
    ): Flow<List<LocalAppSessionEventData>>

    @Query(
        """
        UPDATE LocalAppSessionEventData
        SET requestState = :requestState, responsePayload = :responsePayload, completedAt = :completedAt
        WHERE eventId = :eventId
        """,
    )
    suspend fun updateSessionEventRequestState(
        eventId: String,
        requestState: AppRequestState,
        responsePayload: Encryptable<String>?,
        completedAt: Long?,
    )

    @Query("SELECT * FROM LocalAppSessionEventData WHERE eventId = :eventId")
    fun observeEvent(eventId: String): Flow<LocalAppSessionEventData?>

    @Query("SELECT * FROM LocalAppSessionEventData WHERE eventId = :eventId")
    suspend fun getSessionEvent(eventId: String): LocalAppSessionEventData?

    @Query("DELETE FROM LocalAppSessionEventData WHERE appIdentifier = :appIdentifier")
    suspend fun deleteEvents(appIdentifier: String)

    @Query("SELECT MAX(COALESCE(completedAt, requestedAt)) FROM LocalAppSessionEventData WHERE sessionId = :sessionId")
    suspend fun getLastActivityAt(sessionId: String): Long?
}
