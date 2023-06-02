package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPrimalPaging(
    @SerialName("since") val sinceId: Long,
    @SerialName("until") val untilId: Long,
    @SerialName("order_by") val orderBy: String,
)
