package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.android.profile.utils.combinePremiumInfoIfLegend

@Dao
interface ProfileDataDao {

    @Transaction
    fun insertOrUpdateAll(data: List<ProfileData>) {
        val existingProfiles = findProfileData(data.map { it.ownerId }).associateBy { it.ownerId }
        insertOrReplaceAll(
            data.map { profileData ->
                profileData.combinePremiumInfoIfLegend(existingProfiles[profileData.ownerId])
            },
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(data: List<ProfileData>)

    @Transaction
    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfile(profileId: String): Flow<Profile?>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfileData(profileId: String): Flow<ProfileData?>

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    fun observeProfilesData(profileIds: List<String>): Flow<List<ProfileData>>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun findProfileData(profileId: String): ProfileData?

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    fun findProfileData(profileIds: List<String>): List<ProfileData>

    @Query("SELECT eventId FROM ProfileData WHERE ownerId = :profileId")
    fun findMetadataEventId(profileId: String): String?
}
