package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.account.WalletActivationParams
import net.primal.wallet.data.remote.model.GetActivationCodeRequestBody
import net.primal.wallet.data.remote.model.WalletActivationDetails

internal fun WalletActivationParams.toWalletActivationRequestDTO(): GetActivationCodeRequestBody {
    return GetActivationCodeRequestBody(
        userDetails = WalletActivationDetails(
            firstName = this.firstName,
            lastName = this.lastName,
            email = this.email,
            dateOfBirth = this.dateOfBirth,
            country = this.country,
            state = this.state,
        ),
    )
}
