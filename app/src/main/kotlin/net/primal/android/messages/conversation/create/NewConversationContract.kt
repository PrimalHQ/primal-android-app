package net.primal.android.messages.conversation.create

interface NewConversationContract {
    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onProfileClick: (String) -> Unit,
    )
}
