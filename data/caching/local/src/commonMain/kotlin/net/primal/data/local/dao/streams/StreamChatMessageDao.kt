package net.primal.data.local.dao.streams

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamChatMessageDao {

    @Upsert
    suspend fun upsertAll(data: List<StreamChatMessageData>)

    @Upsert
    suspend fun upsert(data: StreamChatMessageData)

    @Transaction
    @Query(
        """
            SELECT * FROM StreamChatMessageData
            WHERE streamATag = :streamATag
            ORDER BY createdAt ASC
        """,
    )
    fun observeMessages(streamATag: String): Flow<List<StreamChatMessage>>

    @Query("DELETE FROM StreamChatMessageData WHERE streamATag = :streamATag")
    suspend fun deleteMessages(streamATag: String)
}
