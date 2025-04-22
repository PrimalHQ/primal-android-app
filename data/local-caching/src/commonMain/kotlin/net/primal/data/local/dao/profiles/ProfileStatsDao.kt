package net.primal.data.local.dao.profiles

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: ProfileStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<ProfileStats>)

    @Query("SELECT * FROM ProfileStats WHERE profileId = :profileId")
    fun observeProfileStats(profileId: String): Flow<ProfileStats?>

    @Query("SELECT * FROM ProfileStats WHERE profileId IN (:profileIds)")
    suspend fun findProfileStats(profileIds: List<String>): List<ProfileStats>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(data: List<ProfileStats>)
}
