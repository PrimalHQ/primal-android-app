package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class NostrWallet(
    val lud16: String,
    val relayUrl: String,
    val pubkey: String,
    val secret: String,
) {
    fun toStringUrl(): String {
        return "nostr+walletconnect://${pubkey}?relay=${relayUrl}&secret=${secret}&lud16=${lud16}"
    }
}
