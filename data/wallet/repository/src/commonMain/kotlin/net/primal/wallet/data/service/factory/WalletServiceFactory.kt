package net.primal.wallet.data.service.factory

import net.primal.domain.wallet.Wallet
import net.primal.wallet.data.service.WalletService

internal interface WalletServiceFactory {

    fun getServiceForWallet(wallet: Wallet): WalletService<Wallet>
}
