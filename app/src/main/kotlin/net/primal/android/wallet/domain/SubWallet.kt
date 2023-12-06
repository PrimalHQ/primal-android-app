package net.primal.android.wallet.domain

import kotlinx.serialization.Serializable
import net.primal.android.wallet.domain.serializer.SubWalletSerializer

@Serializable(with = SubWalletSerializer::class)
enum class SubWallet(val id: Int) {
    Open(id = 1),
}
