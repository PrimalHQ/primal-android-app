package net.primal.android.wallet.repository

import net.primal.android.crypto.urlToLnUrlHrp
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.utils.ZapConversionUtils.formatAsString
import net.primal.android.wallet.utils.ZapConversionUtils.toBtc
import net.primal.android.wallet.zaps.NostrZapper
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapRequestData

class WalletNostrZapper(
    private val walletRepository: WalletRepository,
) : NostrZapper {

    override suspend fun zap(data: ZapRequestData) {
        try {
            walletRepository.withdraw(
                userId = data.zapperUserId,
                body = WithdrawRequestBody(
                    subWallet = 1,
                    targetLnUrl = data.lnUrlDecoded.urlToLnUrlHrp(),
                    targetPubKey = data.targetUserId,
                    amountBtc = data.zapAmountInSats.toBtc().formatAsString(),
                    noteRecipient = data.zapComment.ifBlank { null },
                    noteSelf = data.zapComment.ifBlank { null },
                    zapRequest = data.userZapRequestEvent,
                ),
            )
        } catch (error: WssException) {
            throw ZapFailureException(cause = error)
        }
    }
}
