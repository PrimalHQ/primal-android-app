package net.primal.data.local.dao.streams

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
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
