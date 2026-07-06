package net.primal.data.local.dao.messages

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import net.primal.domain.messages.ConversationRelation

@Dao
interface MessageConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<MessageConversationData>)

    @Transaction
    @Query(
        """
           SELECT * FROM MessageConversationData
           WHERE relation = :relation AND ownerId = :ownerId ORDER BY lastMessageAt DESC
       """,
    )
    fun newestConversationsPagedByOwnerId(
        relation: ConversationRelation,
        ownerId: String,
    ): PagingSource<Int, MessageConversation>

    @Query(
        """
        UPDATE MessageConversationData SET unreadMessagesCount = 0
        WHERE participantId = :participantId AND ownerId = :ownerId
    """,
    )
    suspend fun markConversationAsRead(participantId: String, ownerId: String)

    @Query("UPDATE MessageConversationData SET unreadMessagesCount = 0 WHERE ownerId = :ownerId")
    suspend fun markAllConversationAsRead(ownerId: String)

    @Query("DELETE FROM MessageConversationData WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)
}
