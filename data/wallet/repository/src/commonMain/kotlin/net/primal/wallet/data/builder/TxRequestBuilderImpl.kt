package net.primal.wallet.data.builder

import net.primal.domain.builder.TxRequestBuilder
import net.primal.domain.nostr.utils.parseAsLNUrlOrNull
import net.primal.domain.wallet.DraftTx
import net.primal.domain.wallet.TxRequest

class TxRequestBuilderImpl : TxRequestBuilder {
    override fun build(draftTx: DraftTx): Result<TxRequest> {
        val lnInvoice = draftTx.lnInvoice
        val lnUrl = draftTx.targetLnUrl
        val parsedLnUrl = draftTx.targetLud16?.parseAsLNUrlOrNull()
        val onChainAddress = draftTx.targetOnChainAddress

        return when {
            lnInvoice != null ->
                Result.success(
                    TxRequest.Lightning.LnInvoice(
                        amountSats = draftTx.amountSats,
                        noteRecipient = draftTx.noteRecipient,
                        noteSelf = draftTx.noteSelf,
                        lnInvoice = lnInvoice,
                    ),
                )

            lnUrl != null ->
                Result.success(
                    TxRequest.Lightning.LnUrl(
                        amountSats = draftTx.amountSats,
                        noteRecipient = draftTx.noteRecipient,
                        noteSelf = draftTx.noteSelf,
                        lnUrl = lnUrl,
                        lud16 = draftTx.targetLud16,
                    ),
                )

            parsedLnUrl != null ->
                Result.success(
                    TxRequest.Lightning.LnUrl(
                        amountSats = draftTx.amountSats,
                        noteRecipient = draftTx.noteRecipient,
                        noteSelf = draftTx.noteSelf,
                        lnUrl = parsedLnUrl,
                        lud16 = draftTx.targetLud16,
                    ),
                )

            onChainAddress != null ->
                Result.success(
                    TxRequest.BitcoinOnChain(
                        amountSats = draftTx.amountSats,
                        noteRecipient = draftTx.noteRecipient,
                        noteSelf = draftTx.noteSelf,
                        onChainAddress = onChainAddress,
                        onChainTier = draftTx.onChainMiningFeeTier,
                    ),
                )

            else -> Result.failure(
                exception = IllegalArgumentException(
                    "Failed to build TxRequest from the given DraftTx. None of the condition were satisfied.",
                ),
            )
        }
    }
}
