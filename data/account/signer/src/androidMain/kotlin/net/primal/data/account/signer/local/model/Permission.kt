package net.primal.data.account.signer.local.model

import kotlinx.serialization.Serializable

@Serializable
data class Permission(
    val type: SignerMethod,
    val kind: Int? = null,
)
