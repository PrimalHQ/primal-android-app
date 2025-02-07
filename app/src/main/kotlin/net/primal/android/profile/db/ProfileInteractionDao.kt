package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileInteractionDao {

    @Upsert
    fun upsert(interaction: ProfileInteraction)

    @Transaction
    @Query(
        """
        SELECT * FROM ProfileInteraction AS PI
        INNER JOIN ProfileData AS PD ON PD.ownerId = PI.profileId
        WHERE PI.ownerId IS :ownerId
        ORDER BY PI.lastInteractionAt DESC
        LIMIT :limit
    """,
    )
    fun observeRecentProfilesByOwnerId(ownerId: String, limit: Int = 10): Flow<List<Profile>>
}
