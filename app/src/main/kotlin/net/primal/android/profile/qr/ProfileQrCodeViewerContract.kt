package net.primal.android.profile.qr

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface ProfileQrCodeViewerContract {

    data class UiState(
        val profileId: String,
        val profileDetails: ProfileDetailsUi? = null,
    )
}
