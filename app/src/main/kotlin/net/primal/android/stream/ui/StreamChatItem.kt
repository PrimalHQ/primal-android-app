package net.primal.android.stream.ui

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.events.ui.EventZapUiModel

sealed interface StreamChatItem {
    val timestamp: Long
    val uniqueId: String

    data class ChatMessageItem(
        val message: ChatMessageUi,
        val isAuthorFollowed: Boolean,
        val isAuthorMuted: Boolean,
        val authorProfileStats: ProfileStatsUi?,
    ) : StreamChatItem {
        override val timestamp: Long = message.timestamp
        override val uniqueId: String = message.messageId
    }

    data class ZapMessageItem(
        val zap: EventZapUiModel,
        val zapperProfile: ProfileDetailsUi?,
        val isZapperFollowed: Boolean,
        val isZapperMuted: Boolean,
        val zapperProfileStats: ProfileStatsUi?,
    ) : StreamChatItem {
        override val timestamp: Long = zap.zappedAt
        override val uniqueId: String = zap.id
    }
}
