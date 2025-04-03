package net.primal.android.signer

import kotlinx.serialization.Serializable

@Serializable
data class Permission(
    val type: SignerMethod,
    val kind: Int? = null,
)
