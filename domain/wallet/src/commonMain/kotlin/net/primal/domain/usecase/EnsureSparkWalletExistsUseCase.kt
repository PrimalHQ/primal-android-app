package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.wallet.SeedPhraseGenerator
import net.primal.domain.wallet.SparkWalletManager

class EnsureSparkWalletExistsUseCase(
    private val sparkWalletManager: SparkWalletManager,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val seedPhraseGenerator: SeedPhraseGenerator,
) {

    suspend fun invoke(userId: String): Result<String> =
        runCatching {
            val existingSeedWords = sparkWalletAccountRepository.getPersistedSeedWords(userId).firstOrNull()
            val isNewWallet = existingSeedWords == null

            val seedWords = existingSeedWords ?: seedPhraseGenerator.generate()
                .getOrThrow()
                .joinToString(separator = " ")

            val walletId = sparkWalletManager.initializeWallet(seedWords).getOrThrow()

            if (isNewWallet) {
                sparkWalletAccountRepository.persistSeedWords(userId, walletId, seedWords).getOrThrow()
            }

            sparkWalletAccountRepository.fetchWalletAccountInfo(userId, walletId)

            walletId
        }
}
