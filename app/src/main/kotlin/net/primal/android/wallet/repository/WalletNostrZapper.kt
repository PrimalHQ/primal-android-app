package net.primal.android.wallet.repository

import javax.inject.Inject
import net.primal.android.crypto.urlToLnUrlHrp
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.utils.CurrencyConversionUtils.formatAsString
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.zaps.NostrZapper
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapRequestData
import net.primal.core.networking.sockets.errors.WssException

class WalletNostrZapper @Inject constructor(
    private val walletRepository: WalletRepository,
) : NostrZapper {

    override suspend fun zap(data: ZapRequestData) {
        try {
            walletRepository.withdraw(
                userId = data.zapperUserId,
                body = WithdrawRequestBody(
                    subWallet = SubWallet.Open,
                    targetLnUrl = data.lnUrlDecoded.urlToLnUrlHrp(),
                    targetPubKey = data.targetUserId,
                    amountBtc = data.zapAmountInSats.toBtc().formatAsString(),
                    noteRecipient = data.zapComment.ifBlank { null },
                    noteSelf = data.zapComment.ifBlank { null },
                    zapRequest = data.userZapRequestEvent,
                ),
            )
        } catch (error: MissingPrivateKey) {
            throw ZapFailureException(cause = error)
        } catch (error: WssException) {
            throw ZapFailureException(cause = error)
        }
    }
}
