package net.primal.data.local.dao.notes

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface FeedPostDataCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun connect(data: List<FeedPostDataCrossRef>)

    @Query(
        """
            SELECT * FROM FeedPostDataCrossRef 
            WHERE feedSpec = :spec AND ownerId = :ownerId 
            ORDER BY position DESC LIMIT 1
        """,
    )
    suspend fun findLastBySpec(ownerId: String, spec: String): FeedPostDataCrossRef?

    @Query("DELETE FROM FeedPostDataCrossRef WHERE feedSpec = :feedSpec AND ownerId = :ownerId")
    suspend fun deleteConnectionsByDirective(ownerId: String, feedSpec: String)

    @Query("DELETE FROM FeedPostDataCrossRef WHERE ownerId = :ownerId")
    suspend fun deleteConnections(ownerId: String)

    @Query("DELETE FROM FeedPostDataCrossRef WHERE eventId = :eventId")
    suspend fun deletePostConnections(eventId: String)
}
