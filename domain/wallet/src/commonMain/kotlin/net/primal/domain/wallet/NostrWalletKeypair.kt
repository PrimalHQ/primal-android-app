package net.primal.domain.wallet

import kotlinx.serialization.Serializable

@Serializable
data class NostrWalletKeypair(
    val privateKey: String,
    val pubkey: String,
)
