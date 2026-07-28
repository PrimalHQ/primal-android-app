package net.primal.data.remote.api.importing.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ImportEventsResponse(
    val imported: Int = 0,
    val errors: Int = 0,
)
