package net.primal.domain.account

import net.primal.core.utils.Result

interface PrimalWalletAccountRepository {

    suspend fun fetchWalletStatus(userId: String): Result<PrimalWalletStatus>
}
