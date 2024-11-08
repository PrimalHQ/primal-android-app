package net.primal.android.premium.home

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership

interface PremiumHomeContract {

    data class UiState(
        val displayName: String = "",
        val avatarCdnImage: CdnImage? = null,
        val profileNostrAddress: String? = null,
        val profileLightningAddress: String? = null,
        val membership: PremiumMembership? = null,
        val error: ApplyError? = null,
    )

    sealed class UiEvent {
        data object CancelSubscription : UiEvent()

        data object ApplyPrimalNostrAddress : UiEvent()
        data object ApplyPrimalLightningAddress : UiEvent()

        data object DismissError : UiEvent()
    }

    sealed class ApplyError {
        data object FailedToApplyNostrAddress : ApplyError()
        data object FailedToApplyLightningAddress : ApplyError()
        data object ProfileMetadataNotFound : ApplyError()
    }
}
