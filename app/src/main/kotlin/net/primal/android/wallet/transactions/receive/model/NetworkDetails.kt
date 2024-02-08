package net.primal.android.wallet.transactions.receive.model

import net.primal.android.wallet.domain.Network

data class NetworkDetails(
    val network: Network,
    val address: String? = null,
    val invoice: String? = null,
) {
    val qrCodeValue: String? get() {
        return invoice ?: address?.let {
            when (network) {
                Network.Lightning -> "lightning:$it"
                Network.Bitcoin -> "bitcoin:$it"
            }
        }
    }

    val copyValue: String? get() = invoice ?: address
}
