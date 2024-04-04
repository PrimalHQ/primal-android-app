package net.primal.android.nostr.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventHintsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: EventHints): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(data: List<EventHints>)

    @Update
    suspend fun update(data: EventHints)

    @Update
    suspend fun update(data: List<EventHints>)

    @Query("SELECT * FROM EventHints WHERE eventId = :eventId")
    suspend fun findById(eventId: String): EventHints?

    @Query("SELECT * FROM EventHints WHERE eventId IN (:eventIds)")
    suspend fun findById(eventIds: List<String>): List<EventHints>

    @Query("UPDATE EventHints SET isBookmarked = NULL")
    suspend fun clearAllBookmarks()
}
