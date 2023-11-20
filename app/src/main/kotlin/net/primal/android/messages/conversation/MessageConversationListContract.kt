package net.primal.android.messages.conversation

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.user.domain.Badges

interface MessageConversationListContract {
    data class UiState(
        val activeRelation: ConversationRelation,
        val conversations: Flow<PagingData<MessageConversationUi>>,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data class ChangeRelation(val relation: ConversationRelation) : UiEvent()
        data object MarkAllConversationsAsRead : UiEvent()
        data object ConversationsSeen : UiEvent()
    }
}
