package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
enum class RelayKind {
    UserRelay,
    NwcRelay,
}
