package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class Credential(
    val nsec: String?,
    val npub: String,
)
