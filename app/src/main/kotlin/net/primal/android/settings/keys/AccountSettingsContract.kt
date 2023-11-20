package net.primal.android.settings.keys

import net.primal.android.attachments.domain.CdnResourceVariant

interface AccountSettingsContract {
    data class UiState(
        val avatarUrl: String? = null,
        val avatarVariants: List<CdnResourceVariant> = emptyList(),
        val nsec: String = "",
        val npub: String = "",
    )
}
