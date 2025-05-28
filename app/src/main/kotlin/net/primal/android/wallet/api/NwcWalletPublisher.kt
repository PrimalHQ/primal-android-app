package net.primal.android.wallet.api

import javax.inject.Inject
class NwcWalletPublisher @Inject constructor()

data class NwcSession(
    val walletPubkey: String,
    val clientSecret: String,
    val relays: List<String>,
)
