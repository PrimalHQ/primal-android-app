package net.primal.android.core.push.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTokenContent(
    val token: String,
)
