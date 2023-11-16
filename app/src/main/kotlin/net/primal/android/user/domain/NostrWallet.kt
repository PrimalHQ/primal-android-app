package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class NostrWallet(
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
