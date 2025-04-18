package net.primal.data.local.dao.events

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.primal.domain.links.EventUriType

@Dao
interface EventUriDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllEventNostrUris(data: List<EventUriNostr>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllEventUris(data: List<EventUri>)

    @Query("SELECT * FROM EventUri WHERE eventId = :noteId AND type IN (:types) ORDER BY position")
    suspend fun loadEventUris(noteId: String, types: List<EventUriType> = EventUriType.entries): List<EventUri>
}
