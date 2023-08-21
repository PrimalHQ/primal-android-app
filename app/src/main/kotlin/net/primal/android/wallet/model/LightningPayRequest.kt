package net.primal.android.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class PayerDataRequirement(
    val mandatory: Boolean,
)

@Serializable
data class PayerData(
    val name: PayerDataRequirement,
    val identifier: PayerDataRequirement? = null,
)

@Serializable
data class LightningPayRequest(
    val allowsNostr: Boolean? = null,
    val commentAllowed: Int? = null,
    val disposable: Boolean? = null,
    val callback: String? = null,
    val minSendable: ULong? = null,
    val maxSendable: ULong? = null,
    val tag: String? = null,
    val metadata: String? = null,
    val nostrPubkey: String? = null,
    val payerData: PayerData? = null,
)
