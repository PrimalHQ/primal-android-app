package net.primal.wallet.data.service.concrete

import net.primal.core.networking.utils.orderByPagingIfNotNull
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.Result
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.runCatching
import net.primal.domain.nostr.utils.ensureEncodedLnUrl
import net.primal.domain.nostr.utils.stripLightningPrefix
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.model.CreateLightningInvoiceRequest
import net.primal.wallet.data.model.Transaction
import net.primal.wallet.data.model.TransactionsRequest
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.DepositRequestBody
import net.primal.wallet.data.remote.model.TransactionsRequestBody
import net.primal.wallet.data.remote.model.WithdrawRequestBody
import net.primal.wallet.data.repository.mappers.remote.asLightingInvoiceResultDO
import net.primal.wallet.data.repository.mappers.remote.mapAsPrimalTransactions
import net.primal.wallet.data.service.WalletService

internal class PrimalWalletServiceImpl(
    private val primalWalletApi: PrimalWalletApi,
) : WalletService {

    override suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult> =
        runCatching {
            val response = primalWalletApi.getBalance(userId = wallet.walletId)

            WalletBalanceResult(
                balanceInBtc = response.amount.toDouble(),
                maxBalanceInBtc = response.maxAmount?.toDouble(),
            )
        }

    override suspend fun fetchTransactions(wallet: Wallet, request: TransactionsRequest): Result<List<Transaction>> =
        runCatching {
            val response = primalWalletApi.getTransactions(
                userId = wallet.walletId,
                body = TransactionsRequestBody(
                    subWallet = SubWallet.Open,
                    until = request.until,
                    since = request.since,
                    minAmountInBtc = request.getIfTypeOrNull(TransactionsRequest.Primal::minAmountInBtc),
                    limit = request.limit,
                ),
            )

            response.transactions.mapAsPrimalTransactions(
                walletId = wallet.walletId,
                userId = wallet.userId,
                walletAddress = wallet.lightningAddress,
            ).orderByPagingIfNotNull(pagingEvent = response.paging) { transactionId }
        }

    override suspend fun createLightningInvoice(
        wallet: Wallet,
        request: CreateLightningInvoiceRequest,
    ): Result<LnInvoiceCreateResult> =
        runCatching {
            require(request is CreateLightningInvoiceRequest.Primal) { "Request was not of type Primal." }
            val response = primalWalletApi.createLightningInvoice(
                userId = wallet.userId,
                body = DepositRequestBody(
                    subWallet = request.subWallet,
                    amountBtc = request.amountInBtc,
                    description = request.description,
                ),
            )

            response.asLightingInvoiceResultDO()
        }

    override suspend fun pay(wallet: Wallet, request: TxRequest): Result<Unit> =
        runCatching {
            primalWalletApi.withdraw(
                userId = wallet.userId,
                body = WithdrawRequestBody(
                    subWallet = SubWallet.Open,
                    targetLud16 = request.getIfTypeOrNull(TxRequest.Lightning.LnUrl::lud16),
                    targetLnUrl = request.getIfTypeOrNull(TxRequest.Lightning.LnUrl::lnUrl)
                        ?.ensureEncodedLnUrl()?.stripLightningPrefix(),
                    targetBtcAddress = request.getIfTypeOrNull(TxRequest.BitcoinOnChain::onChainAddress),
                    onChainTier = request.getIfTypeOrNull(TxRequest.BitcoinOnChain::onChainTier),
                    lnInvoice = request.getIfTypeOrNull(TxRequest.Lightning.LnInvoice::lnInvoice)
                        ?.stripLightningPrefix(),
                    amountBtc = request.amountSats.toLong().toBtc().formatAsString(),
                    noteRecipient = request.noteRecipient,
                    noteSelf = request.noteSelf,
                ),
            )
        }
}
