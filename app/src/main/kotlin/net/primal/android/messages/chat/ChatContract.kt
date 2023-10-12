package net.primal.android.messages.chat

interface ChatContract {
    data class UiState(
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data class MessageSend(val text: String) : UiEvent()
    }
}
