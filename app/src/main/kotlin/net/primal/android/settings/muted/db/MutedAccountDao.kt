package net.primal.android.settings.muted.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MutedAccountDao {
    @Upsert
    fun upsertAll(data: Set<MutedAccount>)

    @Query("SELECT * FROM MutedAccount")
    fun observeAllMuted(): Flow<List<MutedAccount>>

    @Query("DELETE FROM MutedAccount")
    fun deleteAll()
}