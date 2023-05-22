package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Upsert
@Dao
interface PostStatsDao {

    @Upsert
    fun upsertAll(events: List<PostStats>)

}
