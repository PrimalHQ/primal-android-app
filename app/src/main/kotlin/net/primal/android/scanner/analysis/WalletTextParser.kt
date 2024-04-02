package net.primal.android.scanner.analysis

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.crypto.urlToLnUrlHrp
import net.primal.android.navigation.asUrlDecoded
import net.primal.android.wallet.api.parseAsLNUrlOrNull
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import net.primal.android.wallet.utils.isBitcoinAddress
import net.primal.android.wallet.utils.isBitcoinUri
import net.primal.android.wallet.utils.isLightningAddress
import net.primal.android.wallet.utils.isLightningUri
import net.primal.android.wallet.utils.isLnInvoice
import net.primal.android.wallet.utils.isLnUrl
import net.primal.android.wallet.utils.parseBitcoinPaymentInstructions

class WalletTextParser @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val walletRepository: WalletRepository,
) {

    suspend fun parseText(userId: String, text: String): DraftTx? {
        return when (text.parseRecipientType()) {
            WalletRecipientType.LnInvoice -> handleLnInvoiceText(userId, text)

            WalletRecipientType.LnUrl -> handleLnUrlText(userId, text)

            WalletRecipientType.LnAddress -> handleLightningAddressText(userId, text)

            WalletRecipientType.BitcoinAddress,
            WalletRecipientType.BitcoinUri,
            -> handleBitcoinText(text = text)

            null -> null
        }
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
        val response = withContext(dispatchers.io()) {
            val lnbc = text.split(":").last()
            walletRepository.parseLnInvoice(userId = userId, lnbc = lnbc)
        }
        return DraftTx(
            targetUserId = response.userId,
            lnInvoice = text,
            lnInvoiceData = response.lnInvoiceData,
            amountSats = (response.lnInvoiceData.amountMilliSats / 1000).toString(),
            noteRecipient = response.comment.asUrlDecoded(),
        )
    }

    private suspend fun handleLnUrlText(userId: String, text: String): DraftTx {
        val response = withContext(dispatchers.io()) {
            val lnurl = text.split(":").last()
            walletRepository.parseLnUrl(userId = userId, lnurl = lnurl)
        }
        return DraftTx(
            minSendable = response.minSendable,
            maxSendable = response.maxSendable,
            targetUserId = response.targetPubkey,
            targetLud16 = response.targetLud16,
            targetLnUrl = text,
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
