package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class Badges(
    val notifications: Int = 0,
    val messages: Int = 0,
)
