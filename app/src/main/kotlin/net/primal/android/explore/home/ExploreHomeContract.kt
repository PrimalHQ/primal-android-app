package net.primal.android.explore.home

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.user.domain.Badges

interface ExploreHomeContract {
    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val badges: Badges = Badges(),
        val hashtags: List<List<HashtagUi>> = emptyList(),
    )
}
