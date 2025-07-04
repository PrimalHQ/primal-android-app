package net.primal.android.wallet.repository

import io.github.aakira.napier.Napier
import javax.inject.Inject
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.SubWallet
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.nostr.cryptography.utils.urlToLnUrlHrp
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapRequestData
import net.primal.domain.nostr.zaps.ZapResult

class WalletNostrZapper @Inject constructor(
    private val walletRepository: WalletRepository,
) : NostrZapper {

    override suspend fun zap(data: ZapRequestData): ZapResult =
        runCatching {
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
            ZapResult.Success
        }.getOrElse { error ->
            Napier.e(error) { "Failed to withdraw zap." }
            ZapResult.Failure(error = ZapError.FailedToPublishEvent)
        }
}
