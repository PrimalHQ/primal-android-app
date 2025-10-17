package net.primal.android.wallet.dashboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import net.primal.domain.wallet.Wallet

fun Flow<Wallet>.distinctUntilWalletIdChanged(): Flow<Wallet> {
    return this.distinctUntilChanged { old, new -> old.walletId == new.walletId }
}
