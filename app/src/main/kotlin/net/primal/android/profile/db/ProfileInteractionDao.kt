package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileInteractionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(interaction: ProfileInteraction)

    @Query(
        """
        SELECT * FROM ProfileInteraction AS PI
        WHERE PI.ownerId IS :ownerId
        ORDER BY PI.lastInteractionAt DESC
        LIMIT :limit
    """,
    )
    fun observeRecentProfilesByOwnerId(ownerId: String, limit: Int = 10): Flow<List<ProfileInteraction>>

    @Query("DELETE FROM ProfileInteraction WHERE ownerId = :ownerId")
    fun deleteAllByOwnerId(ownerId: String)
}
