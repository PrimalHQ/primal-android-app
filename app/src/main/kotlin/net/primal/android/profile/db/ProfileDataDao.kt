package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<ProfileData>)

    @Transaction
    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfile(profileId: String): Flow<Profile>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun findProfileData(profileId: String): ProfileData

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    fun findProfileData(profileIds: List<String>): List<ProfileData>

    @Query("SELECT eventId FROM ProfileData WHERE ownerId = :profileId")
    fun findMetadataEventId(profileId: String): String
}
