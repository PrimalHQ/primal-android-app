package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.profile.ProfileData
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.NostrTransactionData
import net.primal.wallet.data.local.dao.PrimalTransactionData
import net.primal.wallet.data.local.dao.WalletTransaction as WalletTransactionPO
import net.primal.wallet.data.local.dao.WalletTransactionData

internal fun WalletTransactionPO.toDomain(otherProfile: ProfileData? = null): Transaction =
    when (this.info.walletType) {
        WalletType.PRIMAL -> {
            val primal = this.primal
            require(primal != null) { "PrimalTransactionData is null but the walletType is Primal." }

            Transaction.Primal(
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
                totalFeeInBtc = this.info.totalFeeInBtc,
                walletLightningAddress = primal.walletLightningAddress,
                amountInUsd = primal.amountInUsd,
                isZap = primal.isZap,
                isStorePurchase = primal.isStorePurchase,
                userSubWallet = primal.userSubWallet,
                userLightningAddress = primal.userLightningAddress,
                otherUserId = primal.otherUserId,
                otherLightningAddress = primal.otherLightningAddress,
                exchangeRate = primal.exchangeRate,
                onChainAddress = primal.onChainAddress,
                onChainTxId = primal.onChainTxId,
                zapNoteId = primal.zapNoteId,
                zapNoteAuthorId = primal.zapNoteAuthorId,
                zappedByUserId = primal.zappedByUserId,
                otherUserProfile = otherProfile,
            )
        }

        WalletType.NWC -> {
            val nwc = this.nwc
            require(nwc != null) { "NostrTransactionData is null but the walletType is NWC." }

            Transaction.NWC(
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
                totalFeeInBtc = this.info.totalFeeInBtc,
                preimage = nwc.preimage,
                descriptionHash = nwc.descriptionHash,
                paymentHash = nwc.paymentHash,
                metadata = nwc.metadata,
            )
        }
    }

internal fun Transaction.toWalletTransactionData() =
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

internal fun Transaction.Primal.toPrimalTransactionData() =
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

internal fun Transaction.NWC.toNostrTransactionData() =
    NostrTransactionData(
        transactionId = this.transactionId,
        preimage = this.preimage,
        descriptionHash = this.descriptionHash,
        paymentHash = this.paymentHash,
        metadata = this.metadata,
    )
