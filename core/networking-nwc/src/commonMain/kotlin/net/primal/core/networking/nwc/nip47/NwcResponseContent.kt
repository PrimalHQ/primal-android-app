package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NwcResponseContent<T>(
    @SerialName("result_type") val resultType: String,
    val error: NwcError? = null,
    val result: T? = null,
)
