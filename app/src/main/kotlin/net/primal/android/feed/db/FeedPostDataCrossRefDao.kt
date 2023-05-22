package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface FeedPostDataCrossRefDao {

    @Upsert
    suspend fun connect(refs: List<FeedPostDataCrossRef>)

}
