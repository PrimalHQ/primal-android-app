package net.primal.android.settings.muted.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MutedDao {
    @Upsert
    fun upsertAll(data: Set<Muted>)

    @Query("SELECT * FROM Muted")
    fun observeAllMuted(): Flow<List<Muted>>

    @Query("DELETE FROM Muted")
    fun deleteAll()
}