package net.primal.wallet.data.zaps

import io.github.aakira.napier.Napier
import net.primal.core.lightning.LightningPayHelper
import net.primal.core.utils.MSATS_IN_SATS
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapRequestData
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.WalletRepository

class TsunamiWalletNostrZapper(
    private val lightningPayHelper: LightningPayHelper,
    private val walletRepository: WalletRepository,
) : NostrZapper {

    override suspend fun zap(walletId: String, data: ZapRequestData): ZapResult {
        val zapPayRequest = runCatching {
            lightningPayHelper.fetchPayRequest(data.recipientLnUrlDecoded)
        }.getOrElse {
            Napier.e(it) { "FailedToFetchZapPayRequest." }
            return ZapResult.Failure(error = ZapError.FailedToFetchZapPayRequest(cause = it))
        }

        val invoice = runCatching {
            lightningPayHelper.fetchInvoice(
                payRequest = zapPayRequest,
                amountInMilliSats = data.zapAmountInSats * MSATS_IN_SATS.toULong(),
                comment = data.zapComment,
                zapEvent = data.userZapRequestEvent,
            )
        }.getOrElse {
            Napier.e(it) { "FailedToFetchZapInvoice." }
            return ZapResult.Failure(error = ZapError.FailedToFetchZapInvoice(cause = it))
        }

        runCatching {
            walletRepository.pay(
                walletId = walletId,
                request = TxRequest.Lightning.LnInvoice(
                    amountSats = data.zapAmountInSats.toString(),
                    noteRecipient = null,
                    noteSelf = null,
                    lnInvoice = invoice.invoice,
                ),
            )
        }.getOrElse {
            Napier.e(it) { "FailedToPayInvoice." }
            return ZapResult.Failure(error = ZapError.FailedToPayZap(cause = it))
        }

        return ZapResult.Success
    }
}
