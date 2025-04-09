package net.primal.android.wallet.repository

import javax.inject.Inject
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.SubWallet
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.utils.urlToLnUrlHrp
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.ZapFailureException
import net.primal.domain.nostr.zaps.ZapRequestData

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
        } catch (error: SignatureException) {
            throw ZapFailureException(cause = error)
        } catch (error: WssException) {
            throw ZapFailureException(cause = error)
        }
    }
}
