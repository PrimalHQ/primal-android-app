package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastUpdatedAtResponse(
    @SerialName("updated_at") val lastUpdatedAt: Long,
)
