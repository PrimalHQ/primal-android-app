package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileStatsDao {

    @Upsert
    fun upsert(data: ProfileStats)

    @Query("SELECT * FROM ProfileStats WHERE profileId = :profileId")
    fun observeProfileStats(profileId: String): Flow<ProfileStats>

}
