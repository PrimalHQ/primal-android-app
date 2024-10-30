package net.primal.android.messages.conversation

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.android.messages.domain.ConversationRelation

interface MessageConversationListContract {
    data class UiState(
        val loading: Boolean = false,
        val activeRelation: ConversationRelation,
        val conversations: Flow<PagingData<MessageConversationUi>>,
    )

    sealed class UiEvent {
        data class ChangeRelation(val relation: ConversationRelation) : UiEvent()
        data object MarkAllConversationsAsRead : UiEvent()
        data object ConversationsSeen : UiEvent()
    }
}
