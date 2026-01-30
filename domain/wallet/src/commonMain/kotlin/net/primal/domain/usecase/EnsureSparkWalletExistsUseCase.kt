package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SeedPhraseGenerator
import net.primal.domain.wallet.SparkWalletManager

class EnsureSparkWalletExistsUseCase(
    private val sparkWalletManager: SparkWalletManager,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val seedPhraseGenerator: SeedPhraseGenerator,
) {

    suspend fun invoke(userId: String, setAsActive: Boolean = false): Result<String> =
        runCatching {
            val existingWalletId = sparkWalletAccountRepository.findPersistedWalletId(userId)
            val isNewWallet = existingWalletId == null

            val seedWords = if (isNewWallet) {
                seedPhraseGenerator.generate().getOrThrow()
            } else {
                sparkWalletAccountRepository.getPersistedSeedWords(existingWalletId).getOrThrow()
            }.joinToString(separator = " ")

            val walletId = sparkWalletManager.initializeWallet(seedWords).getOrThrow()

            if (isNewWallet) {
                sparkWalletAccountRepository.persistSeedWords(userId, walletId, seedWords).getOrThrow()
            }

            sparkWalletAccountRepository.fetchWalletAccountInfo(userId, walletId)

            if (setAsActive) {
                walletAccountRepository.setActiveWallet(userId = userId, walletId = walletId)
            }

            walletId
        }
}
