package net.primal.wallet.data.repository.mappers.remote

import net.primal.domain.account.PromoCodeDetails
import net.primal.wallet.data.remote.model.PromoCodeDetailsResponse

internal fun PromoCodeDetailsResponse.toPromoCodeDetailsDO(): PromoCodeDetails {
    return PromoCodeDetails(
        welcomeMessage = this.welcomeMessage,
        preloadedBtc = this.preloadedBtc,
        originPubkey = this.originPubkey,
    )
}
