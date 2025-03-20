package net.primal.android.auth.signer

import kotlinx.serialization.Serializable

@Serializable
data class Permission(
    val type: String,
    val kind: Int? = null,
)
