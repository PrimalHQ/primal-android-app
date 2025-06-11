package net.primal.core.networking.nwc.model

import kotlinx.serialization.Serializable

@Serializable
data class NostrWalletConnect(
    val lightningAddress: String?,
    val relays: List<String>,
    val pubkey: String,
    val keypair: NostrWalletKeypair,
) {
    fun toStringUrl(): String {
        return "nostr+walletconnect://$pubkey" +
            "?relay=${relays.firstOrNull()}" +
            "&secret=${keypair.privateKey}" +
            "&lud16=${lightningAddress ?: ""}"
    }
}

@Serializable
data class NostrWalletKeypair(val privateKey: String, val pubkey: String)
