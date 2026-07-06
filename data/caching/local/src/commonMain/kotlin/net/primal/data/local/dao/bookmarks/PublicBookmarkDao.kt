package net.primal.data.local.dao.bookmarks

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

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
