package net.primal.wallet.data.service

import net.primal.core.networking.nwc.NwcClientFactory
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.utils.CurrencyConversionUtils.msatsToBtc
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletService
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.wallet.data.service.mappers.asNO

class NostrWalletServiceImpl : WalletService {
    override suspend fun fetchWalletBalance(wallet: Wallet): Result<WalletBalanceResult> =
        runCatching {
            require(wallet is Wallet.NWC) { "Wallet is not type NWC but `NostrWalletService` called." }

            val client = NwcClientFactory.createNwcApiClient(
                nwcData = NostrWalletConnect(
                    lightningAddress = wallet.lightningAddress,
                    relays = wallet.relays,
                    pubkey = wallet.walletId,
                    keypair = wallet.keypair.asNO(),
                ),
            )
            val response = client.getBalance().getOrThrow()

            WalletBalanceResult(
                balanceInBtc = response.balance.msatsToBtc(),
                maxBalanceInBtc = null,
            )
        }
}
