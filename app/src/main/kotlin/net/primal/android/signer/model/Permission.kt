package net.primal.android.signer.model

import kotlinx.serialization.Serializable

@Serializable
data class Permission(
    val type: SignerMethod,
    val kind: Int? = null,
)
