package net.primal.android.messages.list

import net.primal.android.user.domain.Badges

interface MessageListContract {
    data class UiState(
        val loading: Boolean = true,
        val activeAccountAvatarUrl: String? = null,
        val badges: Badges = Badges(),
    )
}
