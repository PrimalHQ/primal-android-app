package net.primal.android.stream.ui

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

data class ChatMessageUi(
    val messageId: String,
    val authorProfile: ProfileDetailsUi,
    val content: String,
    val timestamp: Long,
)
