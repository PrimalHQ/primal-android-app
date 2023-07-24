package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface NostrUriDao {

    @Upsert
    fun upsert(data: List<NostrUri>)

}
