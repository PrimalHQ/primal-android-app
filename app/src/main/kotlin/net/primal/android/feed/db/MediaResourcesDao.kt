package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface MediaResourcesDao {

    @Upsert
    fun upsert(data: List<MediaResource>)

}
