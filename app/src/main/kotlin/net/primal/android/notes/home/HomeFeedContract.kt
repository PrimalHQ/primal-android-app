package net.primal.android.notes.home

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.user.domain.Badges

interface HomeFeedContract {

    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data object RequestUserDataUpdate : UiEvent()
    }
}