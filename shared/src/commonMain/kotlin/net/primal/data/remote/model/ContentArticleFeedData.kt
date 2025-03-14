package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentArticleFeedData(
    val name: String,
    val description: String,
    val spec: String,
    val enabled: Boolean,
    @SerialName("feedkind") val feedKind: String,
)
