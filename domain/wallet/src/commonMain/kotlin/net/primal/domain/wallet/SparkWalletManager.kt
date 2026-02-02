package net.primal.domain.wallet

import net.primal.core.utils.Result

interface SparkWalletManager {

    suspend fun initializeWallet(seedWords: String): Result<String>

    suspend fun disconnectWallet(walletId: String): Result<Unit>
}
