package net.primal.android.wallet.utils

import net.primal.core.utils.getIfTypeOrNull
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.capabilities

val Wallet?.shouldShowBackup: Boolean
    get() {
        val supportsBackup = this?.capabilities?.supportsWalletBackup == true
        val balance = this?.balanceInBtc ?: 0.0
        val isBackedUp = getIfTypeOrNull(Wallet.Spark::isBackedUp) == true

        return supportsBackup && balance > 0.0 && !isBackedUp
    }
