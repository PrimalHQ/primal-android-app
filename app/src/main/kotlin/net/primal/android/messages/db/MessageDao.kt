package net.primal.android.messages.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import java.util.UUID

@Dao
interface MessageDao {

    @Upsert
    fun upsertAll(data: List<MessageData>)

    @Query("SELECT * FROM MessageData WHERE participantId = :participantId")
    fun newestMessagesPaged(participantId: UUID): PagingSource<Int, MessageData>

}
