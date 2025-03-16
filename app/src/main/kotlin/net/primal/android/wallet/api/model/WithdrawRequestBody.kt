package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.wallet.domain.SubWallet
import net.primal.domain.nostr.NostrEvent

@Serializable
data class WithdrawRequestBody(
    @SerialName("subwallet") val subWallet: SubWallet,
    @SerialName("target_lud16") val targetLud16: String? = null,
    @SerialName("target_lnurl") val targetLnUrl: String? = null,
    @SerialName("target_pubkey") val targetPubKey: String? = null,
    @SerialName("target_bcaddr") val targetBtcAddress: String? = null,
    @SerialName("onchain_tier") val onChainTier: String? = null,
    @SerialName("lnInvoice") val lnInvoice: String? = null,
    @SerialName("amount_btc") val amountBtc: String? = null,
    @SerialName("note_for_recipient") val noteRecipient: String? = null,
    @SerialName("note_for_self") val noteSelf: String? = null,
    @SerialName("zap_request") val zapRequest: NostrEvent? = null,
) : WalletOperationRequestBody()
