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

    @Query("SELECT * FROM DvmFeedActionUserCrossRef WHERE ownerId = :ownerId")
    fun observeActionUsersByOwner(ownerId: String): Flow<List<DvmFeedActionUserCrossRef>>

    @Upsert
    suspend fun upsertDvmFeedData(data: List<DvmFeedData>)

    @Upsert
    suspend fun upsertRecommendedCrossRefs(refs: List<RecommendedDvmFeedCrossRef>)

    @Upsert
    suspend fun upsertActionUserCrossRefs(refs: List<DvmFeedActionUserCrossRef>)

    @Query("DELETE FROM RecommendedDvmFeedCrossRef WHERE ownerId = :ownerId AND specKindFilter = :specKindFilter")
    suspend fun deleteRecommendedByOwner(ownerId: String, specKindFilter: String)

    @Query("DELETE FROM DvmFeedActionUserCrossRef WHERE ownerId = :ownerId AND dvmEventId IN (:dvmEventIds)")
    suspend fun deleteActionUsersByOwnerAndFeedIds(ownerId: String, dvmEventIds: List<String>)
}
