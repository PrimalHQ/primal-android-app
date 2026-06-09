package net.primal.domain.account

import net.primal.core.utils.Result

interface PrimalWalletAccountRepository {

    suspend fun fetchWalletAccountInfo(userId: String): Result<String>

    suspend fun fetchWalletStatus(userId: String): Result<PrimalWalletStatus>
}
