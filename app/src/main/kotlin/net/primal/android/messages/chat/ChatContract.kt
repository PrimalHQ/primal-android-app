package net.primal.android.messages.chat

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.messages.chat.model.ChatMessageUi

interface ChatContract {
    data class UiState(
        val participantId: String,
        val messages: Flow<PagingData<ChatMessageUi>>,
        val newMessageText: String = "",
        val sending: Boolean = false,
        val error: ChatError? = null,
        val participantProfile: ProfileDetailsUi? = null,
    ) {
        sealed class ChatError {
            data class PublishError(val cause: Throwable?) : ChatError()
            data class MissingRelaysConfiguration(val cause: Throwable) : ChatError()
        }
    }

    sealed class UiEvent {
        data object MessagesSeen : UiEvent()
        data object SendMessage : UiEvent()
        data class UpdateNewMessage(val text: String) : UiEvent()
    }
}
