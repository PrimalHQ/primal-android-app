package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.domain.account.TsunamiWalletAccountRepository
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.tsunami.TsunamiWalletSdk
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.db.WalletDatabase

class TsunamiWalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
    private val tsunamiWalletSdk: TsunamiWalletSdk,
) : TsunamiWalletAccountRepository {

    override suspend fun initializeWallet(userId: String, walletKey: String): Result<String> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                tsunamiWalletSdk.createWallet(walletKey).getOrThrow()
            }
        }

    override suspend fun fetchWalletAccountInfo(userId: String, walletId: String): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                walletDatabase.withTransaction {
                    walletDatabase.wallet().insertOrIgnoreWalletInfo(
                        info = WalletInfo(
                            walletId = walletId,
                            userId = userId.asEncryptable(),
                            lightningAddress = null,
                            type = WalletType.TSUNAMI,
                        ),
                    )
                }
            }
        }

    override suspend fun terminateWallet(walletId: String): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                tsunamiWalletSdk.destroyWallet(walletId).getOrThrow()
            }
        }
}
