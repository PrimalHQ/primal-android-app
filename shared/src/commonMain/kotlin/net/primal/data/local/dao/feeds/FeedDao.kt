package net.primal.data.local.dao.feeds

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.primal.domain.FeedSpecKind

@Dao
interface FeedDao {

    @Query("SELECT * FROM Feed WHERE specKind = :specKind AND ownerId = :ownerId ORDER BY position ASC")
    suspend fun getAllFeedsBySpecKind(ownerId: String, specKind: FeedSpecKind): List<Feed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<Feed>)

    @Query("SELECT * FROM Feed WHERE ownerId = :ownerId ORDER BY position ASC")
    fun observeAllFeeds(ownerId: String): Flow<List<Feed>>

    @Query("SELECT * FROM Feed WHERE ownerId = :ownerId AND specKind = :specKind ORDER BY position ASC")
    fun observeAllFeedsBySpecKind(ownerId: String, specKind: FeedSpecKind): Flow<List<Feed>>

    @Query("SELECT EXISTS(SELECT 1 FROM Feed WHERE ownerId = :ownerId AND spec = :spec)")
    fun observeContainsFeed(ownerId: String, spec: String): Flow<Boolean>

    @Query("DELETE FROM Feed WHERE ownerId = :ownerId AND specKind = :specKind")
    suspend fun deleteAllByOwnerIdAndSpecKind(ownerId: String, specKind: FeedSpecKind)

    @Query("DELETE FROM Feed WHERE ownerId = :ownerId AND spec = :spec")
    suspend fun deleteAllByOwnerIdAndSpec(ownerId: String, spec: String)

    @Query("DELETE FROM Feed WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)
}
