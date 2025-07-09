package net.primal.android.messages.conversation

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.domain.messages.ConversationRelation

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
        data object RefreshConversations : UiEvent()
    }

    data class ScreenCallbacks(
        val onConversationClick: (String) -> Unit,
        val onProfileClick: (String) -> Unit,
        val onNewMessageClick: () -> Unit,
        val onClose: () -> Unit,
    )
}
