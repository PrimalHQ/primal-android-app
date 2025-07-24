package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.profile.ProfileData
import net.primal.domain.transactions.Transaction as TransactionDO
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.NostrTransactionData
import net.primal.wallet.data.local.dao.PrimalTransactionData
import net.primal.wallet.data.local.dao.WalletTransaction as WalletTransactionPO
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.model.Transaction as TransactionDto

internal fun WalletTransactionPO.toDomain(otherProfile: ProfileData? = null): TransactionDO =
    when (this.info.walletType) {
        WalletType.PRIMAL -> fromPrimalTxToDomain(otherProfile = otherProfile)
        WalletType.NWC -> fromNostrTxToDomain(otherProfile = otherProfile)
    }

private fun WalletTransactionPO.fromNostrTxToDomain(otherProfile: ProfileData?) =
    TransactionDO.Lightning(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt,
        userId = this.info.userId,
        note = this.info.note,
        invoice = this.info.invoice,
        amountInBtc = this.info.amountInBtc,
        amountInUsd = this.primal?.amountInUsd,
        exchangeRate = this.primal?.exchangeRate,
        totalFeeInBtc = this.info.totalFeeInBtc,
        otherUserId = this.primal?.otherUserId,
        otherLightningAddress = this.primal?.otherLightningAddress,
        otherUserProfile = otherProfile,
    )

private fun WalletTransactionPO.fromPrimalTxToDomain(otherProfile: ProfileData?): TransactionDO =
    when {
        this.primal?.isZap == true -> this.asZapTransactionDO(otherProfile = otherProfile)

        this.primal?.isStorePurchase == true -> this.asStorePurchaseTransactionDO()

        this.primal?.onChainAddress != null -> this.asOnChainTransactionDO()

        else -> this.asLightningTransaction(otherProfile = otherProfile)
    }

internal fun TransactionDto.toWalletTransactionData() =
    WalletTransactionData(
        transactionId = this.transactionId,
        walletId = this.walletId,
        walletType = this.walletType,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt,
        amountInBtc = this.amountInBtc,
        userId = this.userId,
        note = this.note,
        invoice = this.invoice,
        totalFeeInBtc = this.totalFeeInBtc,
    )

internal fun TransactionDto.Primal.toPrimalTransactionData() =
    PrimalTransactionData(
        transactionId = this.transactionId,
        walletLightningAddress = this.walletLightningAddress,
        amountInUsd = this.amountInUsd,
        isZap = this.isZap,
        isStorePurchase = this.isStorePurchase,
        userSubWallet = this.userSubWallet,
        userLightningAddress = this.userLightningAddress,
        otherUserId = this.otherUserId,
        otherLightningAddress = this.otherLightningAddress,
        exchangeRate = this.exchangeRate,
        onChainAddress = this.onChainAddress,
        onChainTxId = this.onChainTxId,
        zapNoteId = this.zapNoteId,
        zapNoteAuthorId = this.zapNoteAuthorId,
        zappedByUserId = this.zappedByUserId,
    )

internal fun TransactionDto.NWC.toNostrTransactionData() =
    NostrTransactionData(
        transactionId = this.transactionId,
        preimage = this.preimage,
        descriptionHash = this.descriptionHash,
        paymentHash = this.paymentHash,
        metadata = this.metadata,
    )

private fun WalletTransactionPO.asZapTransactionDO(otherProfile: ProfileData?) =
    TransactionDO.Zap(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt,
        userId = this.info.userId,
        note = this.info.note,
        invoice = this.info.invoice,
        amountInBtc = this.info.amountInBtc,
        amountInUsd = this.primal?.amountInUsd,
        exchangeRate = this.primal?.exchangeRate,
        totalFeeInBtc = this.info.totalFeeInBtc,
        otherUserId = this.primal?.otherUserId,
        otherLightningAddress = this.primal?.otherLightningAddress,
        zapNoteId = this.primal?.zapNoteId,
        zapNoteAuthorId = this.primal?.zapNoteAuthorId,
        zappedByUserId = this.primal?.zappedByUserId,
        otherUserProfile = otherProfile,
    )

private fun WalletTransactionPO.asStorePurchaseTransactionDO() =
    TransactionDO.StorePurchase(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt,
        userId = this.info.userId,
        note = this.info.note,
        invoice = this.info.invoice,
        amountInBtc = this.info.amountInBtc,
        amountInUsd = this.primal?.amountInUsd,
        exchangeRate = this.primal?.exchangeRate,
        totalFeeInBtc = this.info.totalFeeInBtc,
    )

private fun WalletTransactionPO.asOnChainTransactionDO() =
    TransactionDO.OnChain(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt,
        userId = this.info.userId,
        note = this.info.note,
        invoice = this.info.invoice,
        amountInBtc = this.info.amountInBtc,
        amountInUsd = this.primal?.amountInUsd,
        exchangeRate = this.primal?.exchangeRate,
        totalFeeInBtc = this.info.totalFeeInBtc,
        onChainTxId = this.primal?.onChainTxId,
        onChainAddress = this.primal?.onChainAddress,
    )

private fun WalletTransactionPO.asLightningTransaction(otherProfile: ProfileData?) =
    TransactionDO.Lightning(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt,
        userId = this.info.userId,
        note = this.info.note,
        invoice = this.info.invoice,
        amountInBtc = this.info.amountInBtc,
        amountInUsd = this.primal?.amountInUsd,
        exchangeRate = this.primal?.exchangeRate,
        totalFeeInBtc = this.info.totalFeeInBtc,
        otherUserId = this.primal?.otherUserId,
        otherLightningAddress = this.primal?.otherLightningAddress,
        otherUserProfile = otherProfile,
    )
