package net.primal.android.messages.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.primal.android.messages.domain.ConversationRelation

@Dao
interface MessageConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<MessageConversationData>)

    @Query("SELECT * FROM MessageConversationData WHERE relation = :relation ORDER BY lastMessageAt DESC")
    fun newestConversationsPaged(relation: ConversationRelation): PagingSource<Int, MessageConversation>

    @Query("SELECT * FROM MessageConversationData WHERE participantId = :participantId")
    fun findConversationByParticipantId(participantId: String): MessageConversation

    @Query("UPDATE MessageConversationData SET unreadMessagesCount = 0 WHERE participantId = :participantId")
    fun markConversationAsRead(participantId: String)

    @Query("UPDATE MessageConversationData SET unreadMessagesCount = 0")
    fun markAllConversationAsRead()
}
