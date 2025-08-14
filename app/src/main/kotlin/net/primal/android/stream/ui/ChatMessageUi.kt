package net.primal.android.stream.ui

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.notes.feed.model.NoteNostrUriUi

data class ChatMessageUi(
    val messageId: String,
    val authorProfile: ProfileDetailsUi,
    val content: String,
    val timestamp: Long,
    val nostrUris: List<NoteNostrUriUi> = emptyList(),
)
