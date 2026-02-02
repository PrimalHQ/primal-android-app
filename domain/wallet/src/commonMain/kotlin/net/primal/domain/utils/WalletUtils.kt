package net.primal.domain.utils

import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletKycLevel

fun Wallet.isPrimalWalletAndActivated() = this is Wallet.Primal && this.kycLevel != WalletKycLevel.None

fun Wallet?.isConfigured(): Boolean = this != null && (this !is Wallet.Primal || this.kycLevel != WalletKycLevel.None)
