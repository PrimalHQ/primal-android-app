package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface ConversationCrossRefDao {

    @Upsert
    fun connect(data: List<ConversationCrossRef>)

}
