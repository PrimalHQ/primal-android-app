package net.primal.android.notes.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedPostDataCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun connect(data: List<FeedPostDataCrossRef>)

    @Query("SELECT MAX(orderIndex) FROM FeedPostDataCrossRef WHERE feedSpec = :feedSpec")
    suspend fun getOrderIndexForFeedSpec(feedSpec: String): Int?

    @Query("DELETE FROM FeedPostDataCrossRef WHERE feedSpec = :feedSpec")
    fun deleteConnectionsByDirective(feedSpec: String)
}
