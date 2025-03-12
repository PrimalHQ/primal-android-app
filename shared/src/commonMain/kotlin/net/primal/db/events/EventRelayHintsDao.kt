package net.primal.db.events

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventRelayHintsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(data: List<EventRelayHints>)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(data: List<EventRelayHints>)

    @Query("SELECT * FROM EventRelayHints WHERE eventId IN (:eventIds)")
    suspend fun findById(eventIds: List<String>): List<EventRelayHints>
}
