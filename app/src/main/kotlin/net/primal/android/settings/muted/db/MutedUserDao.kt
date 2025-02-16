package net.primal.android.settings.muted.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MutedUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: Set<MutedUserData>)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT * FROM MutedUserData 
        INNER JOIN ProfileData ON MutedUserData.userId = ProfileData.ownerId
        WHERE MutedUserData.ownerId = :ownerId
        ORDER BY ProfileData.displayName ASC
        """,
    )
    fun observeMutedUsersByOwnerId(ownerId: String): Flow<List<MutedUser>>

    @Query("SELECT EXISTS(SELECT * FROM MutedUserData WHERE userId = :pubkey AND ownerId = :ownerId)")
    fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String): Flow<Boolean>

    @Query("DELETE FROM MutedUserData WHERE ownerId = :ownerId")
    fun deleteAllByOwnerId(ownerId: String)
}
