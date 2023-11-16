package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface RepostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<RepostData>)
}
