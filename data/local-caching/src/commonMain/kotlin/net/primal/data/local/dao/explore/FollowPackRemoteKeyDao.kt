package net.primal.data.local.dao.explore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FollowPackRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<FollowPackRemoteKey>)

    @Query("SELECT * FROM FollowPackRemoteKey WHERE followPackATag = :aTag")
    suspend fun find(aTag: String): FollowPackRemoteKey?

    @Query("SELECT * FROM FollowPackRemoteKey ORDER BY cachedAt DESC LIMIT 1")
    suspend fun findLatest(): FollowPackRemoteKey?

    @Query("DELETE FROM FollowPackRemoteKey")
    suspend fun deleteAll()
}
