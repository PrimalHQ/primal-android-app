package net.primal.data.local.dao.feeds

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DvmFeedDao {

    @Query(
        """
        SELECT f.* FROM DvmFeedData f
        INNER JOIN RecommendedDvmFeedCrossRef c
            ON c.dvmEventId = f.eventId
        WHERE c.ownerId = :ownerId AND c.specKindFilter = :specKindFilter
        ORDER BY c.position ASC
        """,
    )
    fun observeRecommendedDvmFeedData(ownerId: String, specKindFilter: String): Flow<List<DvmFeedData>>

    @Query("SELECT * FROM DvmFeedFeaturedUserCrossRef WHERE ownerId = :ownerId ORDER BY position ASC")
    fun observeFeaturedUsersByOwner(ownerId: String): Flow<List<DvmFeedFeaturedUserCrossRef>>

    @Upsert
    suspend fun upsertDvmFeedData(data: List<DvmFeedData>)

    @Upsert
    suspend fun upsertRecommendedCrossRefs(refs: List<RecommendedDvmFeedCrossRef>)

    @Upsert
    suspend fun upsertFeaturedUserCrossRefs(refs: List<DvmFeedFeaturedUserCrossRef>)

    @Query("DELETE FROM RecommendedDvmFeedCrossRef WHERE ownerId = :ownerId AND specKindFilter = :specKindFilter")
    suspend fun deleteRecommendedByOwner(ownerId: String, specKindFilter: String)

    @Query("DELETE FROM DvmFeedFeaturedUserCrossRef WHERE ownerId = :ownerId AND dvmEventId IN (:dvmEventIds)")
    suspend fun deleteFeaturedUsersByOwnerAndFeedIds(ownerId: String, dvmEventIds: List<String>)
}
