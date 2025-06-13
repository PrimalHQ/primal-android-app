package net.primal.domain.wallet

data class LnUrlParseResult(
    val minSendable: String? = null,
    val maxSendable: String? = null,
    val description: String? = null,
    val targetPubkey: String? = null,
    val targetLud16: String? = null,
)
