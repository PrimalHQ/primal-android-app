package net.primal.android.explore.home

import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.user.domain.Badges

interface ExploreHomeContract {
    data class UiState(
        val activeAccountAvatarUrl: String? = null,
        val activeAccountAvatarVariants: List<CdnResourceVariant> = emptyList(),
        val badges: Badges = Badges(),
        val hashtags: List<List<HashtagUi>> = emptyList(),
    )
}
