package net.primal.android.thread.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface ConversationCrossRefDao {

    @Upsert
    fun connect(data: List<ConversationCrossRef>)

}
