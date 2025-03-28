package net.primal.data.local.dao.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedPostDataCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun connect(data: List<FeedPostDataCrossRef>)

    @Query("SELECT MAX(orderIndex) FROM FeedPostDataCrossRef WHERE feedSpec = :feedSpec AND ownerId = :ownerId")
    suspend fun getOrderIndexForFeedSpec(ownerId: String, feedSpec: String): Int?

    @Query("DELETE FROM FeedPostDataCrossRef WHERE feedSpec = :feedSpec AND ownerId = :ownerId")
    suspend fun deleteConnectionsByDirective(ownerId: String, feedSpec: String)

    @Query("DELETE FROM FeedPostDataCrossRef WHERE ownerId = :ownerId")
    suspend fun deleteConnections(ownerId: String)
}
