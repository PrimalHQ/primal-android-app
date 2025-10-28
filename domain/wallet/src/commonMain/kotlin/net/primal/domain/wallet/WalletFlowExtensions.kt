package net.primal.domain.wallet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

fun Flow<Wallet?>.distinctUntilWalletIdChanged(): Flow<Wallet?> {
    return this.distinctUntilChanged { old, new -> old?.walletId == new?.walletId }
}
