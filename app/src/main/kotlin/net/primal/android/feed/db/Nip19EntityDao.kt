package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface Nip19EntityDao {

    @Upsert
    fun upsert(data: List<Nip19Entity>)

}
