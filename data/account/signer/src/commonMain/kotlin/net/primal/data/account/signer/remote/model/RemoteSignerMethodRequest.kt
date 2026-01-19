package net.primal.data.account.signer.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteSignerMethodRequest(
    val id: String,
    val method: RemoteSignerMethodType,
    val params: List<String>,
)
