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

    /**
     * Ensures a Spark wallet exists for the given user, creating one if necessary.
     *
     * @param userId The user ID to create/connect the wallet for
     * @param register Whether to register the wallet with the Primal backend.
     *                 Set to false if you want to register later (e.g., to control
     *                 when the 30-second wallet freeze is applied during migration).
     *                 Default is true for normal wallet initialization.
     * @return The wallet ID
     */
    suspend fun invoke(userId: String, register: Boolean = true): Result<String> =
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

            if (register) {
                // Register wallet with server if not already registered
                if (!sparkWalletAccountRepository.isRegistered(walletId)) {
                    sparkWalletAccountRepository.registerSparkWallet(userId, walletId).getOrThrow()
                }
            }

            // Fetch wallet info (LN address) - works if wallet is already registered
            sparkWalletAccountRepository.fetchWalletAccountInfo(userId, walletId).getOrThrow()

            // Select this wallet if no wallet is currently selected
            if (walletAccountRepository.getActiveWallet(userId) == null) {
                walletAccountRepository.setActiveWallet(userId = userId, walletId = walletId)
            }

            walletId
        }
}
