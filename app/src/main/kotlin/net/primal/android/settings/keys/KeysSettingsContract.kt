package net.primal.android.settings.keys

import net.primal.android.attachments.domain.CdnImage

interface KeysSettingsContract {
    data class UiState(
        val avatarCdnImage: CdnImage? = null,
        val nsec: String = "",
        val npub: String = "",
    )
}
