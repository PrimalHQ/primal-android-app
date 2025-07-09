package net.primal.data.local.dao.profiles

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: ProfileStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<ProfileStats>)

    @Query("UPDATE ProfileStats SET followers = :followers WHERE profileId = :profileId")
    suspend fun updateFollowers(profileId: String, followers: Int)

    @Transaction
    suspend fun upsertFollowers(profileId: String, followers: Int) {
        insertOrIgnore(listOf(ProfileStats(profileId = profileId)))
        updateFollowers(profileId = profileId, followers = followers)
    }

    @Query("SELECT * FROM ProfileStats WHERE profileId = :profileId")
    fun observeProfileStats(profileId: String): Flow<ProfileStats?>

    @Query("SELECT * FROM ProfileStats WHERE profileId IN (:profileIds)")
    suspend fun findProfileStats(profileIds: List<String>): List<ProfileStats>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(data: List<ProfileStats>)
}
