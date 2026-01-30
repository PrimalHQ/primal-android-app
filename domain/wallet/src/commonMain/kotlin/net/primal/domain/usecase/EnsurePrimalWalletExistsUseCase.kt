package net.primal.domain.usecase

import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository

class EnsurePrimalWalletExistsUseCase(
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val walletAccountRepository: WalletAccountRepository,
) {

    suspend fun invoke(userId: String, setAsActive: Boolean = false) {
        val status = primalWalletAccountRepository.fetchWalletStatus(userId = userId)
        if (status.hasCustodialWallet && !status.hasMigratedToSparkWallet) {
            primalWalletAccountRepository.fetchWalletAccountInfo(userId = userId)
            if (setAsActive) {
                walletAccountRepository.setActiveWallet(userId = userId, walletId = userId)
            }
        }
    }
}
