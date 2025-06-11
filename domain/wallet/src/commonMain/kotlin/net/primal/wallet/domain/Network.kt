package net.primal.wallet.domain

import kotlinx.serialization.Serializable

@Serializable(with = NetworkSerializer::class)
enum class Network {
    Lightning,
    Bitcoin,
}
