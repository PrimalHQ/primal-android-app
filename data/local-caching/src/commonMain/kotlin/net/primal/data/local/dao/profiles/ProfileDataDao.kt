package net.primal.data.local.dao.profiles

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.domain.premium.PrimalPremiumInfo

@Dao
interface ProfileDataDao {

    @Transaction
    suspend fun insertOrUpdateAll(data: List<ProfileData>) {
        val existingProfiles = findProfileData(data.map { it.ownerId }).associateBy { it.ownerId }
        insertOrReplaceAll(
            data.map { profileData ->
                profileData.combinePremiumInfoIfLegend(existingProfiles[profileData.ownerId])
            },
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(data: List<ProfileData>)

    @Query("SELECT ownerId, primalPremiumInfo FROM ProfileData WHERE ownerId IN (:profileIds)")
    suspend fun findLegendProfileData(
        profileIds: List<String>,
    ): Map<
        @MapColumn("ownerId")
        String,
        @MapColumn("primalPremiumInfo")
        PrimalPremiumInfo?,
        >

    @Transaction
    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfile(profileId: String): Flow<Profile?>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfileData(profileId: String): Flow<ProfileData?>

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    fun observeProfilesData(profileIds: List<String>): Flow<List<ProfileData>>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    suspend fun findProfileData(profileId: String): ProfileData?

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    suspend fun findProfileData(profileIds: List<String>): List<ProfileData>

    @Query("SELECT eventId FROM ProfileData WHERE ownerId = :profileId")
    suspend fun findMetadataEventId(profileId: String): String
}
