package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalPaging(
    @SerialName("order_by") val orderBy: String,
    @SerialName("since") val sinceId: Long? = null,
    @SerialName("until") val untilId: Long? = null,
    @SerialName("elements") val elements: List<String> = emptyList(),
)
