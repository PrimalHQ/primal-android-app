package net.primal.android.wallet.model

sealed class ZapTarget {
    data class Profile(
        val pubkey: String,
        val lnUrl: String,
    ) : ZapTarget()

    data class Note(
        val id: String,
        val authorPubkey: String,
        val authorLnUrl: String,
    ) : ZapTarget()
}
