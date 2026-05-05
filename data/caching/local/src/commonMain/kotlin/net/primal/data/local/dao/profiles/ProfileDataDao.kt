package net.primal.data.local.dao.profiles

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.data.local.db.chunkedFlowQuery
import net.primal.data.local.db.chunkedMapQuery
import net.primal.data.local.db.chunkedQuery
import net.primal.domain.membership.PrimalPremiumInfo

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
    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    suspend fun _findLegendProfileData(
        profileIds: List<String>,
    ): Map<
        @MapColumn("ownerId")
        String,
        @MapColumn("primalPremiumInfo")
        PrimalPremiumInfo?,
        >

    suspend fun findLegendProfileData(profileIds: List<String>): Map<String, PrimalPremiumInfo?> =
        profileIds.chunkedMapQuery { _findLegendProfileData(it) }

    @Query("SELECT ownerId FROM ProfileData WHERE ownerId in (:profileIds)")
    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    suspend fun _findExistingProfileIds(profileIds: List<String>): List<String>

    suspend fun findExistingProfileIds(profileIds: List<String>): List<String> =
        profileIds.chunkedQuery { _findExistingProfileIds(it) }

    @Transaction
    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfile(profileId: String): Flow<Profile?>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfileData(profileId: String): Flow<ProfileData?>

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    fun _observeProfilesData(profileIds: List<String>): Flow<List<ProfileData>>

    fun observeProfilesData(profileIds: List<String>): Flow<List<ProfileData>> =
        profileIds.chunkedFlowQuery { _observeProfilesData(it) }

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    suspend fun findProfileData(profileId: String): ProfileData?

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    suspend fun _findProfileData(profileIds: List<String>): List<ProfileData>

    suspend fun findProfileData(profileIds: List<String>): List<ProfileData> =
        profileIds.chunkedQuery { _findProfileData(it) }

    @Query("SELECT eventId FROM ProfileData WHERE ownerId = :profileId")
    suspend fun findMetadataEventId(profileId: String): String

    @Query("SELECT * FROM ProfileData WHERE lightningAddress = :lightningAddress LIMIT 1")
    suspend fun findProfileDataByLightningAddress(lightningAddress: String): ProfileData?
}
