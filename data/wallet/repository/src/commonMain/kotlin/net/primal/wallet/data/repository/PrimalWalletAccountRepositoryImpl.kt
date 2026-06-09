package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.PrimalWalletStatus
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.PrimalWalletData
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi

internal class PrimalWalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletApi: PrimalWalletApi,
    private val walletDatabase: WalletDatabase,
) : PrimalWalletAccountRepository {

    override suspend fun fetchWalletAccountInfo(userId: String): Result<String> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                val accountInfoResponse = primalWalletApi.getWalletUserInfo(userId)

                val kycLevel = WalletKycLevel.Companion.valueOf(accountInfoResponse.kycLevel)
                    ?: throw IllegalArgumentException("Couldn't parse KycLevel.")

                val lightningAddress = accountInfoResponse.lightningAddress
                walletDatabase.withTransaction {
                    walletDatabase.wallet().insertOrIgnoreWalletInfo(
                        info = WalletInfo(
                            walletId = userId,
                            type = WalletType.PRIMAL,
                        ),
                    )
                    walletDatabase.wallet().insertWalletUserLink(
                        userId = userId,
                        walletId = userId,
                    )
                    walletDatabase.wallet().assignLightningAddress(
                        userId = userId,
                        walletId = userId,
                        lightningAddress = lightningAddress.asEncryptable(),
                    )
                    walletDatabase.wallet().upsertPrimalWalletData(
                        data = PrimalWalletData(
                            walletId = userId,
                            kycLevel = kycLevel,
                        ),
                    )
                }

                userId
            }
        }

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
                    hasCustodialWallet = response.hasCustodialWallet,
                    hasMigratedToSparkWallet = response.hasSparkWallet,
                    lightningAddress = response.lightningAddress,
                    primalWalletDeprecated = response.mustMigrate,
                    registeredSparkWalletId = response.sparkPubkey,
                )
            }
        }
}
