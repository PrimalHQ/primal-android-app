package net.primal.android.premium.manage.content.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentEventKindCount(
    val kind: Int,
    @SerialName("cnt") val count: Long,
)
