package net.primal.android.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class LightningPayRequest(
    val callback: String,
    val metadata: String,
    val minSendable: ULong,
    val maxSendable: ULong,
    val tag: String,
    val allowsNostr: Boolean? = null,
    val nostrPubkey: String? = null,
    val commentAllowed: Int? = null,
    val disposable: Boolean? = null,
    val payerData: PayerData? = null,
)

@Serializable
data class PayerData(
    val name: PayerDataRequirement,
    val identifier: PayerDataRequirement? = null,
)

@Serializable
data class PayerDataRequirement(
    val mandatory: Boolean,
)
