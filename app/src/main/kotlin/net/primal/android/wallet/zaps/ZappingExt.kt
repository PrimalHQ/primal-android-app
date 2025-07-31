package net.primal.android.wallet.zaps

import net.primal.android.user.domain.UserAccount
import net.primal.domain.wallet.WalletKycLevel

fun UserAccount.hasPrimalWallet(): Boolean = primalWallet != null && primalWallet.kycLevel != WalletKycLevel.None
