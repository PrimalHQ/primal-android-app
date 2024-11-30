package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.ext.asMapByKey

@Dao
interface ProfileDataDao {

    @Transaction
    fun upsertAll(data: List<ProfileData>) {
        val existingProfiles = findProfileData(data.map { it.ownerId }).asMapByKey { it.ownerId }

        _upsertAllProfileFields(
            data.map {
                it.copy(
                    primalLegendProfile = it.primalLegendProfile ?: existingProfiles[it.ownerId]?.primalLegendProfile,
                )
            },
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _upsertAllProfileFields(data: List<ProfileData>)

    @Transaction
    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfile(profileId: String): Flow<Profile>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun observeProfileData(profileId: String): Flow<ProfileData>

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    fun observeProfilesData(profileIds: List<String>): Flow<List<ProfileData>>

    @Query("SELECT * FROM ProfileData WHERE ownerId = :profileId")
    fun findProfileData(profileId: String): ProfileData?

    @Query("SELECT * FROM ProfileData WHERE ownerId IN (:profileIds)")
    fun findProfileData(profileIds: List<String>): List<ProfileData>

    @Query("SELECT eventId FROM ProfileData WHERE ownerId = :profileId")
    fun findMetadataEventId(profileId: String): String
}
