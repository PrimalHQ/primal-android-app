package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT COUNT(*) FROM Post")
    fun observeCount(): Flow<Int>

    @Upsert
    fun upsertAll(events: List<Post>)

}