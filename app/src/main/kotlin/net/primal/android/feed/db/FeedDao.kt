package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    @Upsert
    fun upsertAll(data: List<Feed>)

    @Query("SELECT * FROM Feed")
    fun observeAllFeeds(): Flow<List<Feed>>

}