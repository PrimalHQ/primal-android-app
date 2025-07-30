package net.primal.wallet.data.service.concrete

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.core.networking.nwc.NwcClientFactory
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.networking.nwc.nip47.ListTransactionsParams
import net.primal.core.networking.nwc.nip47.LookupInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.MakeInvoiceParams
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.msatsToBtc
import net.primal.core.utils.CurrencyConversionUtils.satsToMSats
import net.primal.core.utils.Result
import net.primal.core.utils.map
import net.primal.core.utils.mapCatching
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.InvoiceType
import net.primal.domain.nostr.lightning.LightningRepository
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.model.CreateLightningInvoiceRequest
import net.primal.wallet.data.model.Transaction
import net.primal.wallet.data.model.TransactionsRequest
import net.primal.wallet.data.service.WalletService
import net.primal.wallet.data.service.mappers.asNO

internal class NostrWalletServiceImpl(
    private val lightningRepository: LightningRepository,
) : WalletService {
    override suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult> =
        runCatching {
            require(wallet is Wallet.NWC) { "Wallet is not type NWC but `NostrWalletService` called." }

            val client = createNwcApiClient(wallet = wallet)
            val response = client.getBalance().getOrThrow()

            WalletBalanceResult(
                balanceInBtc = response.balance.msatsToBtc(),
                maxBalanceInBtc = null,
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun fetchTransactions(wallet: Wallet, request: TransactionsRequest): Result<List<Transaction>> =
        runCatching {
            require(wallet is Wallet.NWC) { "Wallet is not type NWC but `NostrWalletService` called." }
            require(request is TransactionsRequest.NWC) { "Request is not type NWC but `NostrWalletService` called." }

            val client = createNwcApiClient(wallet = wallet)

            client.listTransactions(
                params = ListTransactionsParams(
                    from = request.since,
                    until = request.until,
                    limit = request.limit,
                    offset = request.offset,
                    unpaid = request.unpaid,
                    type = request.type,
                ),
            ).map { response ->
                response.transactions.map { transaction ->
                    Transaction.NWC(
                        transactionId = transaction.paymentHash ?: transaction.invoice ?: Uuid.random().toString(),
                        walletId = wallet.walletId,
                        walletType = WalletType.NWC,
                        type = when (transaction.type) {
                            InvoiceType.Incoming -> TxType.DEPOSIT
                            InvoiceType.Outgoing -> TxType.WITHDRAW
                        },
                        state = transaction.resolveState(),
                        createdAt = transaction.createdAt,
                        updatedAt = transaction.settledAt ?: transaction.createdAt,
                        completedAt = transaction.settledAt,
                        userId = wallet.userId,
                        note = transaction.description,
                        invoice = transaction.invoice,
                        amountInBtc = transaction.amount.msatsToBtc(),
                        totalFeeInBtc = transaction.feesPaid.msatsToBtc().formatAsString(),
                        preimage = transaction.preimage,
                        descriptionHash = transaction.descriptionHash,
                        paymentHash = transaction.paymentHash,
                        metadata = transaction.metadata?.encodeToJsonString(),
                    )
                }
            }.getOrThrow()
        }

    override suspend fun createLightningInvoice(
        wallet: Wallet,
        request: CreateLightningInvoiceRequest,
    ): Result<LnInvoiceCreateResult> =
        runCatching {
            require(wallet is Wallet.NWC) { "Wallet is not type NWC but `NostrWalletService` called." }
            require(
                request is CreateLightningInvoiceRequest.NWC,
            ) { "Request is not type NWC but `NostrWalletService` called." }

            val client = createNwcApiClient(wallet = wallet)

            client.makeInvoice(
                params = MakeInvoiceParams(
                    amount = request.amountInMSats,
                    description = request.description,
                    descriptionHash = request.descriptionHash,
                    expiry = request.expiry,
                ),
            ).map { result ->
                LnInvoiceCreateResult(
                    invoice = result.invoice ?: throw IllegalArgumentException("Didn't receive invoice in response."),
                    description = result.description,
                )
            }.getOrThrow()
        }

    override suspend fun pay(wallet: Wallet, request: TxRequest): Result<Unit> =
        runCatching {
            require(wallet is Wallet.NWC) { "Wallet is not type NWC but `NostrWalletService` called." }
            require(request is TxRequest.Lightning) { "Only lightning transactions are supported through NWC wallet." }

            val client = createNwcApiClient(wallet = wallet)

            val amountMSats = request.amountSats.toLong().satsToMSats()
            val lnInvoice = when (request) {
                is TxRequest.Lightning.LnInvoice -> request.lnInvoice
                is TxRequest.Lightning.LnUrl -> resolveLnInvoice(
                    lnUrl = request.lnUrl,
                    amountMSats = amountMSats,
                    comment = request.noteRecipient,
                ).getOrThrow()
            }

            return client.payInvoice(
                params = PayInvoiceParams(
                    invoice = lnInvoice,
                    amount = amountMSats,
                ),
            ).map { }
        }

    private suspend fun resolveLnInvoice(
        lnUrl: String,
        amountMSats: Long,
        comment: String?,
    ): Result<String> =
        lightningRepository.getPayRequest(lnUrl = lnUrl)
            .mapCatching {
                lightningRepository
                    .getInvoice(
                        callbackUrl = it.callback,
                        amountMSats = amountMSats,
                        comment = comment?.take(it.commentAllowed)?.ifBlank { null },
                    )
                    .getOrThrow()
            }
            .map { it.invoice }

    private fun LookupInvoiceResponsePayload.resolveState(): TxState =
        when {
            settledAt != null -> TxState.SUCCEEDED

            else -> TxState.CREATED
        }

    private fun createNwcApiClient(wallet: Wallet.NWC) =
        NwcClientFactory.createNwcApiClient(
            nwcData = NostrWalletConnect(
                lightningAddress = wallet.lightningAddress,
                relays = wallet.relays,
                pubkey = wallet.pubkey,
                keypair = wallet.keypair.asNO(),
            ),
        )
}
