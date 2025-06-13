package net.primal.domain.wallet

import kotlinx.serialization.Serializable

@Serializable(with = NetworkSerializer::class)
enum class Network {
    Lightning,
    Bitcoin,
}
