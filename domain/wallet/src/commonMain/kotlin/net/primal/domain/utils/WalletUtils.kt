package net.primal.domain.utils

import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletKycLevel

fun Wallet.isActivePrimalWallet() = this is Wallet.Primal && this.kycLevel != WalletKycLevel.None

fun Wallet?.isConfigured(): Boolean =
    this != null && (this.isActivePrimalWallet() || this is Wallet.NWC || this is Wallet.Tsunami)
