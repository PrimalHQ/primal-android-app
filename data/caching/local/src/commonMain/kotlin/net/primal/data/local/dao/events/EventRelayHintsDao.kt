package net.primal.data.local.dao.events

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import net.primal.data.local.db.chunkedQuery

@Dao
interface EventRelayHintsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(data: List<EventRelayHints>)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(data: List<EventRelayHints>)

    @Query("SELECT * FROM EventRelayHints WHERE eventId IN (:eventIds)")
    @Suppress("ktlint:standard:function-naming")
    suspend fun _findById(eventIds: List<String>): List<EventRelayHints>

    suspend fun findById(eventIds: List<String>): List<EventRelayHints> = eventIds.chunkedQuery { _findById(it) }
}
