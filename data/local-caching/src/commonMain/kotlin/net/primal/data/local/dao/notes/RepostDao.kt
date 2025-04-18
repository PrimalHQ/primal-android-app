package net.primal.data.local.dao.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface RepostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<RepostData>)
}
