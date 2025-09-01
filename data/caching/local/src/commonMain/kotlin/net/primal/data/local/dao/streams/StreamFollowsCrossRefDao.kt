package net.primal.data.local.dao.streams

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamFollowsCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: StreamFollowsCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<StreamFollowsCrossRef>)

    @Query("DELETE FROM StreamFollowsCrossRef WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)

    @Query("DELETE FROM StreamFollowsCrossRef WHERE streamATag = :aTag")
    suspend fun deleteByATag(aTag: String)

    @Transaction
    @Query(
        """
        SELECT s.* FROM StreamFollowsCrossRef r 
        INNER JOIN StreamData s ON r.streamATag = s.aTag 
        WHERE r.ownerId = :ownerId
        ORDER BY s.startsAt DESC
        """,
    )
    fun observeStreamByOwnerId(ownerId: String): Flow<List<Stream>>
}
