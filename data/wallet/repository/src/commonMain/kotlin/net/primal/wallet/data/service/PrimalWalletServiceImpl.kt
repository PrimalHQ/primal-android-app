package net.primal.wallet.data.service

import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletService
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.remote.api.PrimalWalletApi

class PrimalWalletServiceImpl(
    private val primalWalletApi: PrimalWalletApi,
) : WalletService {

    override suspend fun fetchWalletBalance(wallet: Wallet): WalletBalanceResult {
        val response = primalWalletApi.getBalance(userId = wallet.walletId)

        return WalletBalanceResult(
            balanceInBtc = response.amount.toDouble(),
            maxBalanceInBtc = response.maxAmount?.toDouble(),
        )
    }
}
