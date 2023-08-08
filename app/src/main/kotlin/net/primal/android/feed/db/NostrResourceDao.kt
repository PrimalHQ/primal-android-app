package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface NostrResourceDao {

    @Upsert
    fun upsertAll(data: List<NostrResource>)

}
