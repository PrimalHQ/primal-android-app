package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface ProfileStatsDao {

    @Upsert
    fun upsert(data: ProfileStats)

    @Upsert
    fun upsertAll(data: List<ProfileStats>)

}
