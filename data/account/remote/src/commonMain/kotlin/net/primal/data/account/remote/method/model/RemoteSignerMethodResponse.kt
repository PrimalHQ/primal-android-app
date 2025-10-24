package net.primal.data.account.remote.method.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteSignerMethodResponse(
    val id: String,
    val result: String,
    val error: String? = null,
)
