package net.primal.data.repository.nip05

import kotlinx.serialization.Serializable

@Serializable
data class Nip05WellKnownResponse(
    val names: Map<String, String> = emptyMap(),
)
