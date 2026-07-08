package net.primal.data.local.dao.explore

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
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
