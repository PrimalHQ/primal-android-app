package net.primal.android.nostr.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update

@Dao
interface EventHintsDao {

    @Insert
    suspend fun insert(data: EventHints)

    @Update
    suspend fun update(data: EventHints)
}
