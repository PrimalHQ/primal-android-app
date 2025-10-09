package net.primal.wallet.data.service

import net.primal.core.lightning.LightningPayHelper
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.MSATS_IN_SATS
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.nostr.utils.decodeLNUrlOrNull
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.tsunami.TsunamiWalletSdk
import net.primal.wallet.data.model.Transaction

internal class TsunamiWalletServiceImpl(
    private val tsunamiWalletSdk: TsunamiWalletSdk,
    private val lightningPayHelper: LightningPayHelper,
) : WalletService<Wallet.Tsunami> {

    override suspend fun fetchWalletBalance(wallet: Wallet.Tsunami): Result<WalletBalanceResult> =
        runCatching {
            val balance = tsunamiWalletSdk.getBalance(walletId = wallet.walletId).getOrThrow()
            WalletBalanceResult(
                balanceInBtc = balance.toDouble(),
                maxBalanceInBtc = null,
            )
        }

    override suspend fun fetchTransactions(
        wallet: Wallet.Tsunami,
        request: TransactionsRequest,
    ): Result<List<Transaction>> =
        runCatching {
            emptyList()
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

    override suspend fun pay(wallet: Wallet.Tsunami, request: TxRequest): Result<Unit> =
        runCatching {
            when (request) {
                is TxRequest.BitcoinOnChain -> throw NotImplementedError()

                is TxRequest.Lightning.LnInvoice -> {
                    tsunamiWalletSdk.payInvoice(
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

                    tsunamiWalletSdk.payInvoice(
                        walletId = wallet.walletId,
                        invoice = lnInvoice.pr,
                    ).getOrThrow()
                }
            }
        }
}
