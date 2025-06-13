package net.primal.domain.wallet

import net.primal.domain.nostr.NostrEvent

data class WalletPayParams(
    val userId: String,
    val subWallet: SubWallet,
    val targetLud16: String? = null,
    val targetLnUrl: String? = null,
    val targetPubKey: String? = null,
    val targetBtcAddress: String? = null,
    val onChainTier: String? = null,
    val lnInvoice: String? = null,
    val amountBtc: String? = null,
    val noteRecipient: String? = null,
    val noteSelf: String? = null,
    val zapRequest: NostrEvent? = null,
)
