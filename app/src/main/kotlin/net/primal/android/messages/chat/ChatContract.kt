package net.primal.android.messages.chat

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.messages.chat.model.ChatMessageUi

interface ChatContract {
    data class UiState(
        val participantId: String,
        val messages: Flow<PagingData<ChatMessageUi>>,
        val participantProfile: ProfileDetailsUi? = null,
        val participantMediaResources: List<MediaResourceUi> = emptyList(),
    )

    sealed class UiEvent {
        data class MessageSend(val text: String) : UiEvent()
    }
}
