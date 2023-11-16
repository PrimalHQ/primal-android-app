package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedPostDataCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun connect(data: List<FeedPostDataCrossRef>)

    @Query("DELETE FROM FeedPostDataCrossRef WHERE feedDirective = :feedDirective")
    fun deleteConnectionsByDirective(feedDirective: String)
}
