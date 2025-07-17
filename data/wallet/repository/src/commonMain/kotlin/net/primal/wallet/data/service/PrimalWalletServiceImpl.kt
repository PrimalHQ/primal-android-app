package net.primal.wallet.data.service

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletService
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.remote.api.PrimalWalletApi

class PrimalWalletServiceImpl(
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
}
