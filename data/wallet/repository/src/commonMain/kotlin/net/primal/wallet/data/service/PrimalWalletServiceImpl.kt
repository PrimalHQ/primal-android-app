package net.primal.wallet.data.service

import io.github.aakira.napier.Napier
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.Result
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.runCatching
import net.primal.domain.nostr.utils.ensureEncodedLnUrl
import net.primal.domain.nostr.utils.stripLightningPrefix
import net.primal.domain.rates.fees.OnChainTransactionFeeTier
import net.primal.domain.wallet.LightningPaymentResult
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.Network
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.PayResult
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.TransactionsPage
import net.primal.domain.wallet.TransactionsRequest
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.DepositRequestBody
import net.primal.wallet.data.remote.model.TransactionsRequestBody
import net.primal.wallet.data.remote.model.WithdrawRequestBody
import net.primal.wallet.data.repository.mappers.remote.asLightingInvoiceResultDO
import net.primal.wallet.data.repository.mappers.remote.asOnChainTxFeeTierDO
import net.primal.wallet.data.repository.mappers.remote.mapAsPrimalTransactions

internal class PrimalWalletServiceImpl(
    private val primalWalletApi: PrimalWalletApi,
) : WalletService<Wallet.Primal> {

    private companion object {
        private const val TAG = "PrimalWalletService"
        private const val DEFAULT_PAGE_SIZE = 50
    }

    override suspend fun fetchWalletBalance(wallet: Wallet.Primal): Result<WalletBalanceResult> =
        runCatching {
            val response = primalWalletApi.getBalance(userId = wallet.walletId)

            WalletBalanceResult(
                balanceInBtc = response.amount.toDouble(),
                maxBalanceInBtc = response.maxAmount?.toDouble(),
            )
        }

    override suspend fun subscribeToWalletBalance(wallet: Wallet.Primal): Flow<WalletBalanceResult> {
        return primalWalletApi.subscribeToBalance(userId = wallet.walletId).map {
            WalletBalanceResult(
                balanceInBtc = it.amount.toDouble(),
                maxBalanceInBtc = it.maxAmount?.toDouble(),
            )
        }
    }

    override suspend fun fetchTransactions(
        wallet: Wallet.Primal,
        request: TransactionsRequest,
    ): Result<TransactionsPage> =
        runCatching {
            val response = primalWalletApi.getTransactions(
                userId = wallet.walletId,
                body = TransactionsRequestBody(
                    subWallet = SubWallet.Open,
                    until = request.until,
                    since = request.since,
                    minAmountInBtc = request.getIfTypeOrNull(TransactionsRequest::minAmountInBtc),
                    limit = request.limit,
                ),
            )

            val transactions = response.transactions.mapAsPrimalTransactions(
                walletId = wallet.walletId,
                userId = wallet.userId,
            )

            val requestedLimit = request.limit ?: DEFAULT_PAGE_SIZE
            val serverCursor = response.paging?.sinceId
            val nextCursor = serverCursor ?: if (transactions.size >= requestedLimit) {
                // Server didn't provide a cursor but we got a full page — compute fallback
                transactions.minOfOrNull { it.createdAt }
            } else {
                null
            }

            Napier.d(tag = TAG) {
                "Fetched ${transactions.size} txs (limit=$requestedLimit), " +
                    "paging.sinceId=$serverCursor, nextCursor=$nextCursor"
            }

            TransactionsPage(
                transactions = transactions,
                nextCursor = nextCursor,
            )
        }

    override suspend fun createLightningInvoice(
        wallet: Wallet.Primal,
        request: LnInvoiceCreateRequest,
    ): Result<LnInvoiceCreateResult> =
        runCatching {
            val response = primalWalletApi.createLightningInvoice(
                userId = wallet.userId,
                body = DepositRequestBody(
                    subWallet = SubWallet.Open,
                    amountBtc = request.amountInBtc,
                    description = request.description,
                ),
            )

            response.asLightingInvoiceResultDO()
        }

    override suspend fun createOnChainAddress(wallet: Wallet.Primal): Result<OnChainAddressResult> =
        runCatching {
            val response = primalWalletApi.createOnChainAddress(
                userId = wallet.userId,
                body = DepositRequestBody(subWallet = SubWallet.Open, network = Network.Bitcoin),
            )
            OnChainAddressResult(address = response.onChainAddress)
        }

    override suspend fun pay(wallet: Wallet.Primal, request: TxRequest): Result<PayResult> =
        runCatching {
            primalWalletApi.withdraw(
                userId = wallet.userId,
                body = WithdrawRequestBody(
                    subWallet = SubWallet.Open,
                    targetLud16 = request.getIfTypeOrNull(TxRequest.Lightning.LnUrl::lud16),
                    targetLnUrl = request.getIfTypeOrNull(TxRequest.Lightning.LnUrl::lnUrl)
                        ?.ensureEncodedLnUrl()?.stripLightningPrefix(),
                    targetBtcAddress = request.getIfTypeOrNull(TxRequest.BitcoinOnChain::onChainAddress),
                    onChainTier = request.getIfTypeOrNull(TxRequest.BitcoinOnChain::onChainTierId),
                    lnInvoice = request.getIfTypeOrNull(TxRequest.Lightning.LnInvoice::lnInvoice)
                        ?.stripLightningPrefix(),
                    amountBtc = request.amountSats.toLong().toBtc().formatAsString(),
                    noteRecipient = request.noteRecipient,
                    noteSelf = request.noteSelf,
                ),
            )
            PayResult()
        }

    override suspend fun fetchMiningFees(
        wallet: Wallet.Primal,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<List<OnChainTransactionFeeTier>> =
        runCatching {
            primalWalletApi.getMiningFees(
                userId = wallet.userId,
                onChainAddress = onChainAddress,
                amountInBtc = amountInBtc,
            ).map { it.asOnChainTxFeeTierDO() }
        }

    override suspend fun awaitLightningPayment(
        wallet: Wallet.Primal,
        invoice: String?,
        timeout: Duration,
    ): Result<LightningPaymentResult> = Result.failure(UnsupportedOperationException("Not supported for Primal wallet"))
}
