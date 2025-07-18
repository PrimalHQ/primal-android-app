package net.primal.wallet.data.service.concrete

import net.primal.core.networking.utils.orderByPagingIfNotNull
import net.primal.core.utils.Result
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.runCatching
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.model.Transaction
import net.primal.wallet.data.model.TransactionsRequest
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.TransactionsRequestBody
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
}
