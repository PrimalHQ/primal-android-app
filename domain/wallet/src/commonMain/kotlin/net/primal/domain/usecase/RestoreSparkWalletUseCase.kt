package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.fold
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
            val deleteResult = sparkWalletAccountRepository.deleteSparkWalletByUserId(userId = userId)

            val newWalletId = deleteResult.fold(
                onSuccess = { oldWalletId ->
                    sparkWalletManager.disconnectWallet(walletId = oldWalletId)
                    sparkWalletManager.initializeWallet(seedWords = seedWords).getOrThrow()
                },
                onFailure = {
                    sparkWalletManager.initializeWallet(seedWords = seedWords).getOrThrow()
                },
            )

            sparkWalletAccountRepository.persistSeedWords(
                userId = userId,
                seedWords = seedWords,
                walletId = newWalletId,
            )
            sparkWalletAccountRepository.registerSparkWallet(userId = userId, walletId = newWalletId)
            sparkWalletAccountRepository.fetchWalletAccountInfo(userId = userId, walletId = newWalletId)
            sparkWalletAccountRepository.markWalletAsBackedUp(walletId = newWalletId)
            walletAccountRepository.setActiveWallet(userId = userId, walletId = newWalletId)

            newWalletId
        }
}
