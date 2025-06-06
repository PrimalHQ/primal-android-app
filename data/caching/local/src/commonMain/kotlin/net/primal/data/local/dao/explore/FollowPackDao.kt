package net.primal.data.local.dao.explore

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowPackDao {

    @Query("DELETE FROM FollowPackProfileCrossRef WHERE followPackATag in (:aTags)")
    suspend fun clearCrossRefsByPackATags(aTags: List<String>)

    @Transaction
    @Query(
        """
        SELECT fp.* FROM FollowPackData fp
        INNER JOIN FollowPackListCrossRef c
            ON c.followPackATag = fp.aTag
        ORDER BY c.position ASC
        """,
    )
    fun getFollowPacks(): PagingSource<Int, FollowPack>

    @Upsert
    suspend fun upsertCrossRefs(refs: List<FollowPackProfileCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFollowPackData(data: List<FollowPackData>)

    @Transaction
    @Query("SELECT * FROM FollowPackData WHERE identifier = :identifier AND authorId = :authorId")
    suspend fun getFollowPack(authorId: String, identifier: String): FollowPack?

    @Transaction
    @Query("SELECT * FROM FollowPackData WHERE identifier = :identifier AND authorId = :authorId")
    fun observeFollowPack(authorId: String, identifier: String): Flow<FollowPack?>
}
