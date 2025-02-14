package net.primal.android.messages.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import net.primal.android.messages.domain.ConversationRelation

@Dao
interface MessageConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<MessageConversationData>)

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
    fun markConversationAsRead(participantId: String, ownerId: String)

    @Query("UPDATE MessageConversationData SET unreadMessagesCount = 0 WHERE ownerId = :ownerId")
    fun markAllConversationAsRead(ownerId: String)
}
