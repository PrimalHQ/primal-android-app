package net.primal.android.events.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.primal.android.events.domain.EventUriType

@Dao
interface EventUriDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllEventNostrUris(data: List<EventUriNostr>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllEventUris(data: List<EventUri>)

    @Query("SELECT * FROM EventUri WHERE eventId = :noteId AND type IN (:types) ORDER BY position")
    fun loadEventUris(noteId: String, types: List<EventUriType> = EventUriType.entries): List<EventUri>
}
