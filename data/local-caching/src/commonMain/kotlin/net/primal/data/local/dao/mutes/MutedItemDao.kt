package net.primal.data.local.dao.mutes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MutedItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: Set<MutedItemData>)

    @Query("DELETE FROM MutedItemData WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT * FROM MutedItemData m
        INNER JOIN ProfileData ON m.item = ProfileData.ownerId
        WHERE m.ownerId = :ownerId AND m.type = 'User'
        ORDER BY ProfileData.displayName ASC
        """,
    )
    fun observeMutedUsersByOwnerId(ownerId: String): Flow<List<MutedUser>>

    @Query("SELECT EXISTS(SELECT * FROM MutedItemData WHERE item = :pubkey AND ownerId = :ownerId)")
    fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String): Flow<Boolean>

    @Query("SELECT * FROM MutedItemData WHERE ownerId = :ownerId AND type = :type")
    fun observeMutedItemsByType(ownerId: String, type: MutedItemType): Flow<List<MutedItemData>>

    @Query("SELECT item FROM MutedItemData WHERE ownerId = :ownerId AND type = 'User'")
    fun observeMutedProfileIdsByOwnerId(ownerId: String): Flow<List<String>>
}
