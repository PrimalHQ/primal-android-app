package net.primal.data.local.dao.bookmarks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PublicBookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmarks(data: List<PublicBookmark>)

    @Query("DELETE FROM PublicBookmark WHERE ownerId = :userId")
    suspend fun deleteAllBookmarks(userId: String)

    @Query("DELETE FROM PublicBookmark WHERE tagValue = :tagValue")
    suspend fun deleteByTagValue(tagValue: String)

    @Query("SELECT * FROM PublicBookmark WHERE tagValue = :tagValue")
    suspend fun findByTagValue(tagValue: String): PublicBookmark?
}
