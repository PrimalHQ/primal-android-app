package net.primal.wallet.data.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.primal.core.lightning.LightningPayHelper
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.MSATS_IN_SATS
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.utils.decodeLNUrlOrNull
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.tsunami.TsunamiWalletSdk
import net.primal.tsunami.model.OnChainTransactionFeePriority
import net.primal.tsunami.model.Transfer
import net.primal.wallet.data.model.Transaction
import net.primal.wallet.data.repository.mappers.remote.mapAsWalletTransaction

internal class TsunamiWalletServiceImpl(
    private val tsunamiWalletSdk: TsunamiWalletSdk,
    private val lightningPayHelper: LightningPayHelper,
    private val eventRepository: EventRepository,
) : WalletService<Wallet.Tsunami> {

    private companion object {
        private const val DEFAULT_OFFSET = 0
        private const val DEFAULT_LIMIT = 100
        private const val DEFAULT_ORDER = "descending"
    }

    override suspend fun fetchWalletBalance(wallet: Wallet.Tsunami): Result<WalletBalanceResult> =
        runCatching {
            val balance = tsunamiWalletSdk.getBalance(walletId = wallet.walletId).getOrThrow()
            WalletBalanceResult(
                balanceInBtc = balance.toDouble(),
                maxBalanceInBtc = null,
            )
        }

    override suspend fun subscribeToWalletBalance(wallet: Wallet.Tsunami): Flow<WalletBalanceResult> {
        return emptyFlow()
    }

    override suspend fun fetchTransactions(
        wallet: Wallet.Tsunami,
        request: TransactionsRequest,
    ): Result<List<Transaction>> =
        runCatching {
            tsunamiWalletSdk.getTransfers(
                walletId = wallet.walletId,
                offset = (request.offset ?: DEFAULT_OFFSET).toULong(),
                limit = (request.limit ?: DEFAULT_LIMIT).toULong(),
                order = DEFAULT_ORDER,
            ).map { transfers ->
                val invoices = transfers.extractAllLnInvoices()
                val zapReceiptsMap = eventRepository.getZapReceipts(invoices = invoices).getOrNull()

                transfers.map {
                    val txInvoice = it.lightningSendRequest?.encodedInvoice
                        ?: it.lightningReceiveRequest?.encodedInvoice
                    val zapRequest = zapReceiptsMap?.get(txInvoice)

                    it.mapAsWalletTransaction(
                        userId = wallet.userId,
                        walletId = wallet.walletId,
                        walletAddress = wallet.lightningAddress,
                        zapRequest = zapRequest,
                    )
                }
            }.getOrThrow()
        }

    private fun List<Transfer>.extractAllLnInvoices(): List<String> {
        val outgoingInvoices = this.mapNotNull { it.lightningSendRequest?.encodedInvoice }
        val incomingInvoices = this.mapNotNull { it.lightningReceiveRequest?.encodedInvoice }
        return outgoingInvoices + incomingInvoices
    }

    override suspend fun createLightningInvoice(
        wallet: Wallet.Tsunami,
        request: LnInvoiceCreateRequest,
    ): Result<LnInvoiceCreateResult> =
        runCatching {
            val amountInBtc = request.amountInBtc
            requireNotNull(amountInBtc) { "Amount is required for tsunami lightning invoices." }

            val lnInvoice = tsunamiWalletSdk.createInvoice(
                walletId = wallet.walletId,
                amountSats = amountInBtc.toDouble().toSats().toULong(),
            ).getOrThrow()

            LnInvoiceCreateResult(
                invoice = lnInvoice,
                description = null,
            )
        }

    override suspend fun createOnChainAddress(wallet: Wallet.Tsunami): Result<OnChainAddressResult> =
        runCatching {
            val address = tsunamiWalletSdk.createOnChainDepositAddress(walletId = wallet.walletId).getOrThrow()
            OnChainAddressResult(address = address)
        }

    override suspend fun pay(wallet: Wallet.Tsunami, request: TxRequest): Result<Unit> =
        runCatching {
            when (request) {
                is TxRequest.BitcoinOnChain -> {
                    tsunamiWalletSdk.payOnChain(
                        walletId = wallet.walletId,
                        withdrawalAddress = request.onChainAddress,
                        feePriority = OnChainTransactionFeePriority.Medium,
                        amountSats = request.amountSats.toULong(),
                    )
                }

                is TxRequest.Lightning.LnInvoice -> {
                    tsunamiWalletSdk.payLightning(
                        walletId = wallet.walletId,
                        invoice = request.lnInvoice,
                    ).getOrThrow()
                }

                is TxRequest.Lightning.LnUrl -> {
                    val lnUrlDecoded = request.lnUrl.decodeLNUrlOrNull()
                    require(lnUrlDecoded != null)

                    val lnInvoice = runCatching {
                        val lnPayRequest = lightningPayHelper.fetchPayRequest(lnUrlDecoded)
                        lightningPayHelper.fetchInvoice(
                            payRequest = lnPayRequest,
                            amountInMilliSats = request.amountSats.toULong() * MSATS_IN_SATS.toULong(),
                            comment = request.noteRecipient ?: "",
                        )
                    }.getOrThrow()

                    tsunamiWalletSdk.payLightning(
                        walletId = wallet.walletId,
                        invoice = lnInvoice.invoice,
                    ).getOrThrow()
                }
            }
        }
}
