package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.domain.TxState
import net.primal.android.wallet.domain.TxType

@Serializable
data class ContentWalletTransaction(
    val id: String,
    val type: TxType,
    val state: TxState,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("completed_at") val completedAt: Long,
    @SerialName("amount_btc") val amountInBtc: String,
    @SerialName("amount_usd") val amountInUsd: String?,
    @SerialName("pubkey_1") val selfPubkey: String,
    @SerialName("subindex_1") val selfSubWallet: SubWallet,
    @SerialName("lud16_1") val selfLud16: String?,
    @SerialName("pubkey_2") val otherPubkey: String?,
    @SerialName("subindex_2") val otherSubWallet: SubWallet?,
    @SerialName("lud16_2") val otherLud16: String?,
    val note: String?,
    @SerialName("is_zap") val isZap: Boolean,
    @SerialName("zap_request") val zapRequestRawJson: String?,
    @SerialName("is_in_app_purchase") val isInAppPurchase: Boolean,
)
