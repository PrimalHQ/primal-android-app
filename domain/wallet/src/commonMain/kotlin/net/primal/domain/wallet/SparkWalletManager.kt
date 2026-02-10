package net.primal.domain.wallet

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result

interface SparkWalletManager {

    val unclaimedDeposits: Flow<UnclaimedDepositEvent>

    val balanceChanged: Flow<String>

    suspend fun initializeWallet(seedWords: String): Result<String>

    suspend fun disconnectWallet(walletId: String): Result<Unit>
}

data class UnclaimedDeposit(
    val txid: String,
    val amountSats: Long,
)

data class UnclaimedDepositEvent(
    val walletId: String,
    val deposits: List<UnclaimedDeposit>,
)
