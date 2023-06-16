package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface FeedPostDataCrossRefDao {

    @Upsert
    fun connect(data: List<FeedPostDataCrossRef>)

    @Query("DELETE FROM FeedPostDataCrossRef WHERE feedDirective = :feedDirective")
    fun deleteConnectionsByDirective(feedDirective: String)

}
