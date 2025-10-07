package net.primal.domain.usecase

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.account.TsunamiWalletAccountRepository

class CreateTsunamiWalletUseCase(
    private val tsunamiWalletAccountRepository: TsunamiWalletAccountRepository,
) {

    suspend fun invoke(userId: String, walletKey: String): Result<String> =
        runCatching {
            val walletId = tsunamiWalletAccountRepository.initializeWallet(userId, walletKey).getOrThrow()
            tsunamiWalletAccountRepository.fetchWalletAccountInfo(userId, walletId)
            walletId
        }
}
