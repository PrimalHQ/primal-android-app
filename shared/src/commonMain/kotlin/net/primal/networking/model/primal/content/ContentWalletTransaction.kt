package net.primal.networking.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO Fix missing domain classes
@Serializable
data class ContentWalletTransaction(
    val id: String,
//    val type: TxType,
//    val state: TxState,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long = createdAt,
    @SerialName("completed_at") val completedAt: Long? = null,
    @SerialName("amount_btc") val amountInBtc: Double,
    @SerialName("amount_usd") val amountInUsd: Double? = null,
    @SerialName("pubkey_1") val selfPubkey: String,
//    @SerialName("subindex_1") val selfSubWallet: SubWallet,
    @SerialName("lud16_1") val selfLud16: String? = null,
    @SerialName("pubkey_2") val otherPubkey: String? = null,
//    @SerialName("subindex_2") val otherSubWallet: SubWallet? = null,
    @SerialName("lud16_2") val otherLud16: String? = null,
    val note: String? = null,
    @SerialName("is_zap") val isZap: Boolean,
    @SerialName("zap_request") val zapRequestRawJson: String? = null,
    @SerialName("is_in_app_purchase") val isInAppPurchase: Boolean,
    val invoice: String? = null,
    @SerialName("total_fee_btc") val totalFeeInBtc: String? = null,
    @SerialName("exchange_rate") val exchangeRate: String? = null,
    @SerialName("onchainAddress") val onChainAddress: String? = null,
    @SerialName("onchain_transaction_id") val onChainTxId: String? = null,
)
