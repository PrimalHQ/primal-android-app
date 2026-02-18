package net.primal.wallet.data.local.dao.nwc

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NwcPendingEventDao {
    @Upsert
    suspend fun upsertAll(data: List<NwcPendingEventData>)

    @Query("DELETE FROM NwcPendingEventData WHERE eventId IN (:eventIds)")
    suspend fun deleteByIds(eventIds: List<String>)

    @Query("SELECT * FROM NwcPendingEventData WHERE userId = :userId")
    fun observeAllByUserId(userId: String): Flow<List<NwcPendingEvent>>

    @Query("DELETE FROM NwcPendingEventData WHERE createdAt < :minCreatedAt")
    suspend fun deleteEventsOlderThan(minCreatedAt: Long)

    @Query("DELETE FROM NwcPendingEventData WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)

    @Query("DELETE FROM NwcPendingEventData WHERE connectionId IN (:connectionIds)")
    suspend fun deleteAllByConnectionIds(connectionIds: List<String>)
}
