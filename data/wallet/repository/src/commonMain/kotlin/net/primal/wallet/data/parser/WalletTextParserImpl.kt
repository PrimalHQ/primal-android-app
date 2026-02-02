import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.Result
import net.primal.core.utils.asUrlDecoded
import net.primal.domain.nostr.cryptography.utils.urlToLnUrlHrp
import net.primal.domain.nostr.utils.parseAsLNUrlOrNull
import net.primal.domain.parser.WalletTextParser
import net.primal.domain.utils.isBitcoinAddress
import net.primal.domain.utils.isBitcoinUri
import net.primal.domain.utils.isLightningAddress
import net.primal.domain.utils.isLightningUri
import net.primal.domain.utils.isLnInvoice
import net.primal.domain.utils.isLnUrl
import net.primal.domain.utils.parseBitcoinPaymentInstructions
import net.primal.domain.wallet.DraftTx
import net.primal.domain.wallet.WalletRepository

class WalletTextParserImpl(
    private val walletRepository: WalletRepository,
) : WalletTextParser {

    override suspend fun parseAndQueryText(userId: String, text: String): Result<DraftTx> {
        return runCatching {
            when (text.parseRecipientType()) {
                WalletRecipientType.LnInvoice -> handleLnInvoiceText(userId, text)

                WalletRecipientType.LnUrl -> handleLnUrlText(userId, text)

                WalletRecipientType.LnAddress -> handleLightningAddressText(userId, text)

                WalletRecipientType.BitcoinAddress,
                WalletRecipientType.BitcoinUri,
                -> handleBitcoinText(text = text)

                null -> null
            }
        }.getOrNull()
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalArgumentException("Provided text was not a valid wallet type text."))
    }

    private fun String.parseRecipientType(): WalletRecipientType? {
        return when {
            isLnInvoice() -> WalletRecipientType.LnInvoice
            isLnUrl() -> WalletRecipientType.LnUrl
            isLightningAddress() -> WalletRecipientType.LnAddress
            isLightningUri() -> {
                val path = this.split(":").last()
                path.parseLightningRecipientType()
            }

            isBitcoinAddress() -> WalletRecipientType.BitcoinAddress
            isBitcoinUri() -> {
                val parsedBitcoinUri = this.parseBitcoinPaymentInstructions()
                return parsedBitcoinUri?.lightning?.parseLightningRecipientType() ?: WalletRecipientType.BitcoinUri
            }

            else -> null
        }
    }

    private fun String.parseLightningRecipientType(): WalletRecipientType? =
        when {
            this.isLightningAddress() -> WalletRecipientType.LnAddress
            this.isLnUrl() -> WalletRecipientType.LnUrl
            this.isLnInvoice() -> WalletRecipientType.LnInvoice
            else -> null
        }

    private suspend fun handleLnInvoiceText(userId: String, text: String): DraftTx {
        val lnbc = text.split(":").last()
        val response = walletRepository.parseLnInvoice(userId = userId, lnbc = lnbc)

        return DraftTx(
            targetUserId = response.userId,
            lnInvoice = lnbc,
            lnInvoiceAmountMilliSats = response.amountMilliSats,
            lnInvoiceDescription = response.description,
            amountSats = ((response.amountMilliSats ?: 0) / 1000).toString(),
            noteRecipient = response.comment?.asUrlDecoded(),
        )
    }

    private suspend fun handleLnUrlText(userId: String, text: String): DraftTx {
        val lnurl = text.split(":").last()
        val response = walletRepository.parseLnUrl(userId = userId, lnurl = lnurl)

        return DraftTx(
            minSendable = response.minSendable,
            maxSendable = response.maxSendable,
            targetUserId = response.targetPubkey,
            targetLud16 = response.targetLud16,
            targetLnUrl = lnurl,
            noteRecipient = response.description,
        )
    }

    private suspend fun handleLightningAddressText(userId: String, text: String): DraftTx? {
        val lud16Value = text.split(":").last()
        val lnUrl = lud16Value.parseAsLNUrlOrNull()?.urlToLnUrlHrp() ?: return null
        return handleLnUrlText(userId = userId, text = lnUrl)
    }

    private fun handleBitcoinText(text: String): DraftTx? {
        val btcInstructions = text.parseBitcoinPaymentInstructions() ?: return null
        return DraftTx(
            targetOnChainAddress = btcInstructions.address,
            onChainInvoice = if (btcInstructions.hasParams()) text else null,
            amountSats = btcInstructions.amount?.toSats()?.toString() ?: "0",
            noteRecipient = btcInstructions.label,
        )
    }

    private enum class WalletRecipientType {
        LnInvoice,
        LnUrl,
        LnAddress,
        BitcoinAddress,
        BitcoinUri,
    }
}
