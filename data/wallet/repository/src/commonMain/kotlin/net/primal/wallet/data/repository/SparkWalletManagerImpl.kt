package net.primal.wallet.data.repository

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.wallet.SparkWalletManager
import net.primal.wallet.data.spark.BreezSdkInstanceManager
import net.primal.wallet.data.validator.RecoveryPhraseValidator

internal class SparkWalletManagerImpl(
    private val breezSdkInstanceManager: BreezSdkInstanceManager,
) : SparkWalletManager {

    private val recoveryPhraseValidator = RecoveryPhraseValidator()

    override suspend fun initializeWallet(seedWords: String): Result<String> =
        runCatching {
            if (!recoveryPhraseValidator.isValid(seedWords)) {
                error("Invalid recovery phrase: expected 12, 15, 18, 21, or 24 valid BIP39 words.")
            }
            breezSdkInstanceManager.createWallet(seedWords)
        }

    override suspend fun disconnectWallet(walletId: String): Result<Unit> =
        runCatching {
            breezSdkInstanceManager.removeInstance(walletId)
        }
}
