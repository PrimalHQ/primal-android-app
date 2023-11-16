package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface ProfileStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: ProfileStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<ProfileStats>)
}
