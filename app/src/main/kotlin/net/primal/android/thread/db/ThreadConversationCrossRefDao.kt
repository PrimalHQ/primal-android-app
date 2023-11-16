package net.primal.android.thread.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface ThreadConversationCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun connect(data: List<ThreadConversationCrossRef>)
}
