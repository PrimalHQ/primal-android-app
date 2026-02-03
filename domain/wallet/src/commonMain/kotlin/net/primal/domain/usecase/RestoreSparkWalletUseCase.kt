package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.fold
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SparkWalletManager

class RestoreSparkWalletUseCase(
    private val sparkWalletManager: SparkWalletManager,
    private val walletAccountRepository: WalletAccountRepository,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
) {
    suspend fun invoke(seedWords: String, userId: String): Result<String> =
        sparkWalletAccountRepository.deleteSparkWalletByUserId(userId = userId)
            .fold(
                onSuccess = { oldWalletId ->
                    sparkWalletManager.disconnectWallet(walletId = oldWalletId)
                    sparkWalletManager.initializeWallet(seedWords = seedWords).getOrThrow()
                },
                onFailure = {
                    sparkWalletManager.initializeWallet(seedWords = seedWords).getOrThrow()
                },
            ).let { newWalletId ->
                sparkWalletAccountRepository.persistSeedWords(
                    userId = userId,
                    seedWords = seedWords,
                    walletId = newWalletId,
                )
                sparkWalletAccountRepository.markWalletAsBackedUp(walletId = newWalletId)
                sparkWalletAccountRepository.fetchWalletAccountInfo(userId = userId, walletId = newWalletId)
                walletAccountRepository.setActiveWallet(userId = userId, walletId = newWalletId)

                newWalletId.asSuccess()
            }
}
