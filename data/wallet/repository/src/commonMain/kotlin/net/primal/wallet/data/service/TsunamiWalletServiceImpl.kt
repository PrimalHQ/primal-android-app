package net.primal.wallet.data.service

import net.primal.core.utils.CurrencyConversionUtils.btcToMSats
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
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
                amountSats = amountInBtc.toDouble().btcToMSats().toULong(),
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

                is TxRequest.Lightning.LnUrl -> throw NotImplementedError()
            }
        }
}
