package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository

class EnsurePrimalWalletExistsUseCase(
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val walletAccountRepository: WalletAccountRepository,
) {

    suspend fun invoke(userId: String, setAsActive: Boolean = false): Result<String?> =
        runCatching {
            val status = primalWalletAccountRepository.fetchWalletStatus(userId = userId).getOrThrow()
            if (status.hasCustodialWallet && !status.hasMigratedToSparkWallet) {
                primalWalletAccountRepository.fetchWalletAccountInfo(userId = userId).getOrThrow()
                if (setAsActive) {
                    walletAccountRepository.setActiveWallet(userId = userId, walletId = userId)
                }
                userId
            } else {
                null
            }
        }
}
