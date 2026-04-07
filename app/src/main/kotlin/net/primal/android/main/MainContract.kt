package net.primal.android.main

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.domain.links.CdnImage

interface MainContract {

    data class UiState(
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val badges: Badges = Badges(),
    )

    sealed class UiEvent {
        data object RequestUserDataUpdate : UiEvent()
    }
}
