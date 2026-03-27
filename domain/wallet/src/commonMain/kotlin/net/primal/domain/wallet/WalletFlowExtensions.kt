package net.primal.domain.wallet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

fun Flow<UserWallet?>.distinctUntilWalletIdChanged(): Flow<UserWallet?> {
    return this.distinctUntilChanged { old, new -> old?.wallet?.walletId == new?.wallet?.walletId }
}
