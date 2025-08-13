package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.wallet.SubWallet
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class PrimalTransactionData(
    @PrimaryKey
    val transactionId: String,
    val walletLightningAddress: Encryptable<String>,
    val amountInUsd: Encryptable<Double>?,
    val isZap: Boolean,
    val isStorePurchase: Boolean,
    val userSubWallet: SubWallet,
    val userLightningAddress: Encryptable<String>?,
    val otherUserId: Encryptable<String>?,
    val otherLightningAddress: Encryptable<String>?,
    val exchangeRate: Encryptable<String>?,
    val onChainAddress: Encryptable<String>?,
    val onChainTxId: Encryptable<String>?,
    val zapNoteId: Encryptable<String>?,
    val zapNoteAuthorId: Encryptable<String>?,
    val zappedByUserId: Encryptable<String>?,
)
