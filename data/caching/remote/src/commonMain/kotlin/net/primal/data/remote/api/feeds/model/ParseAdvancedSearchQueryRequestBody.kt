package net.primal.data.remote.api.feeds.model

import kotlinx.serialization.Serializable

@Serializable
data class ParseAdvancedSearchQueryRequestBody(
    val query: String,
)
