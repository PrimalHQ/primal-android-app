package net.primal.domain

import kotlinx.serialization.Serializable

@Serializable
data class ContentZapDefault(
    val amount: Long,
    val message: String,
)

val DEFAULT_ZAP_DEFAULT = ContentZapDefault(amount = 42L, message = "Onward \uD83E\uDEE1")
