package net.primal.android.wallet.transactions.receive.model

import net.primal.domain.nostr.cryptography.utils.urlToLnUrlHrp
import net.primal.domain.wallet.Network
import net.primal.wallet.data.remote.parseAsLNUrlOrNull

data class NetworkDetails(
    val network: Network,
    val address: String? = null,
    val invoice: String? = null,
) {
    val qrCodeValue: String?
        get() {
            return invoice?.let {
                when (network) {
                    Network.Lightning -> "lightning:$it"
                    Network.Bitcoin -> it
                }
            } ?: address?.let {
                when (network) {
                    Network.Lightning -> "lightning:${it.parseAsLNUrlOrNull()?.urlToLnUrlHrp()}"
                    Network.Bitcoin -> "bitcoin:$it"
                }
            }
        }

    val copyValue: String?
        get() {
            return invoice ?: address?.let {
                when (network) {
                    Network.Lightning -> it.parseAsLNUrlOrNull()?.urlToLnUrlHrp()
                    Network.Bitcoin -> it
                }
            }
        }
}
