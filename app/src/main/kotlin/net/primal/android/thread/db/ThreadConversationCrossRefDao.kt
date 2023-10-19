package net.primal.android.thread.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface ThreadConversationCrossRefDao {

    @Upsert
    fun connect(data: List<ThreadConversationCrossRef>)

}
