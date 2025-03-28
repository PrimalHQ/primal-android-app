package net.primal.android.user.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileInteractionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(interaction: UserProfileInteraction)

    @Query(
        """
        SELECT * FROM UserProfileInteraction AS PI
        WHERE PI.ownerId IS :ownerId
        ORDER BY PI.lastInteractionAt DESC
        LIMIT :limit
    """,
    )
    fun observeRecentProfilesByOwnerId(ownerId: String, limit: Int = 10): Flow<List<UserProfileInteraction>>

    @Query("DELETE FROM UserProfileInteraction WHERE ownerId = :ownerId")
    fun deleteAllByOwnerId(ownerId: String)
}
