package net.primal.android.settings.muted.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MutedUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: Set<MutedUserData>)

    @Query(
        """
        SELECT * FROM MutedUserData 
        INNER JOIN ProfileData ON MutedUserData.userId = ProfileData.ownerId
        ORDER BY ProfileData.displayName ASC
        """,
    )
    fun observeMutedUsers(): Flow<List<MutedUser>>

    @Query("SELECT EXISTS(SELECT * FROM MutedUserData WHERE userId = :pubkey)")
    fun observeIsUserMuted(pubkey: String): Flow<Boolean>

    @Query("DELETE FROM MutedUserData")
    fun deleteAll()
}
