package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SparkWalletManager

class RestoreSparkWalletUseCase(
    private val sparkWalletManager: SparkWalletManager,
    private val walletAccountRepository: WalletAccountRepository,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
) {
    suspend fun invoke(seedWords: String, userId: String): Result<String> =
        runCatching {
            val newWalletId = sparkWalletManager.initializeWallet(seedWords = seedWords).getOrThrow()

            sparkWalletAccountRepository.persistSeedWords(
                walletId = newWalletId,
                seedWords = seedWords,
            ).onFailure { sparkWalletManager.disconnectWallet(newWalletId) }
                .getOrThrow()

            sparkWalletAccountRepository.registerSparkWallet(userId = userId, walletId = newWalletId)
                .onFailure { sparkWalletManager.disconnectWallet(newWalletId) }
                .getOrThrow()

            sparkWalletAccountRepository.fetchWalletAccountInfo(userId = userId, walletId = newWalletId)
                .onFailure {
                    sparkWalletAccountRepository.ensureWalletInfoExists(userId = userId, walletId = newWalletId)
                }
            sparkWalletAccountRepository.markWalletAsBackedUp(walletId = newWalletId)
            walletAccountRepository.setActiveWallet(userId = userId, walletId = newWalletId)

            disconnectOtherWallets(userId = userId, excludeWalletId = newWalletId)

            newWalletId
        }

    private suspend fun disconnectOtherWallets(userId: String, excludeWalletId: String) {
        val existingWalletIds = sparkWalletAccountRepository.findAllPersistedWalletIds(userId)
        for (walletId in existingWalletIds) {
            if (walletId != excludeWalletId && sparkWalletManager.hasInstance(walletId)) {
                sparkWalletManager.disconnectWallet(walletId)
            }
        }
    }
}
