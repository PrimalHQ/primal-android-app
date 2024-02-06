package net.primal.android.wallet.domain

import kotlinx.serialization.Serializable
import net.primal.android.wallet.domain.serializer.NetworkSerializer

@Serializable(with = NetworkSerializer::class)
enum class Network {
    Lightning,
    Bitcoin,
}
