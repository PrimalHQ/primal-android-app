package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.PrimalWalletStatus
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi

internal class PrimalWalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletApi: PrimalWalletApi,
    private val walletDatabase: WalletDatabase,
) : PrimalWalletAccountRepository {

    override suspend fun fetchWalletStatus(userId: String): Result<PrimalWalletStatus> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                val response = primalWalletApi.getWalletStatus(userId)

                val sparkWalletId = response.sparkPubkey
                val lightningAddress = response.lightningAddress

                if (sparkWalletId != null) {
                    walletDatabase.wallet().insertWalletUserLink(
                        userId = userId,
                        walletId = sparkWalletId,
                    )
                    if (lightningAddress != null) {
                        walletDatabase.wallet().assignLightningAddress(
                            userId = userId,
                            walletId = sparkWalletId,
                            lightningAddress = lightningAddress.asEncryptable(),
                        )
                    }
                }

                PrimalWalletStatus(
                    hasMigratedToSparkWallet = response.hasSparkWallet,
                    lightningAddress = response.lightningAddress,
                    registeredSparkWalletId = response.sparkPubkey,
                )
            }
        }
}
