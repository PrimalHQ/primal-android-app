package net.primal.data.account.local.dao.apps.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalAppSessionEventDataDao {
    @Insert
    suspend fun insert(data: LocalAppSessionEventData)

    @Query(
        """
        SELECT * FROM LocalAppSessionEventData
        WHERE sessionId = :sessionId AND (requestState = 'Approved' OR requestState = 'Rejected')
        ORDER BY processedAt DESC
    """,
    )
    fun observeCompletedEventsBySessionId(sessionId: String): Flow<List<LocalAppSessionEventData>>

    @Query("SELECT * FROM LocalAppSessionEventData WHERE eventId = :eventId")
    fun observeEvent(eventId: String): Flow<LocalAppSessionEventData?>

    @Query("SELECT * FROM LocalAppSessionEventData WHERE eventId = :eventId")
    suspend fun getSessionEvent(eventId: String): LocalAppSessionEventData?

    @Query("DELETE FROM LocalAppSessionEventData WHERE appIdentifier = :appIdentifier")
    suspend fun deleteEvents(appIdentifier: String)
}
