package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface PostResourcesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(data: List<PostResource>)

}
