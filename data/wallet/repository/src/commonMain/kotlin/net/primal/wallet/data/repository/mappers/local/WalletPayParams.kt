package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.wallet.WalletPayParams
import net.primal.wallet.data.remote.model.WithdrawRequestBody

internal fun WalletPayParams.toWithdrawRequestDTO(): WithdrawRequestBody {
    return WithdrawRequestBody(
        subWallet = this.subWallet,
        targetLud16 = this.targetLud16,
        targetLnUrl = this.targetLnUrl,
        targetPubKey = this.targetPubKey,
        targetBtcAddress = this.targetBtcAddress,
        onChainTier = this.onChainTier,
        lnInvoice = this.lnInvoice,
        amountBtc = this.amountBtc,
        noteRecipient = this.noteRecipient,
        noteSelf = this.noteSelf,
        zapRequest = this.zapRequest,
    )
}
