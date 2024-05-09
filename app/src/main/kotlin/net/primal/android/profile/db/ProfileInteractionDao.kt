package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileInteractionDao {

    @Query("SELECT * FROM ProfileInteraction WHERE profileId = :profileId")
    fun getInteractionByProfileId(profileId: String?): ProfileInteraction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(interaction: ProfileInteraction)

    @Update
    fun update(interaction: ProfileInteraction)

    @Transaction
    fun insertOrUpdate(interaction: ProfileInteraction) {
        val existingProfile = getInteractionByProfileId(interaction.profileId)
        if (existingProfile != null) {
            update(interaction)
        } else {
            insert(interaction)
        }
    }

    @Transaction
    @Query(
        """
        SELECT * FROM ProfileData AS PD
        INNER JOIN ProfileInteraction AS PI ON PD.ownerId = PI.profileId
        ORDER BY PI.lastInteractionAt DESC
        LIMIT :limit
    """,
    )
    fun observeRecentProfiles(limit: Int = 10): Flow<List<Profile>>
}
