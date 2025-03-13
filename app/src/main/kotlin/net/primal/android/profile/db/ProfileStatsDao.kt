package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(data: ProfileStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<ProfileStats>)

    @Query("SELECT * FROM ProfileStats WHERE profileId = :profileId")
    fun observeProfileStats(profileId: String): Flow<ProfileStats?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(data: List<ProfileStats>)
}
