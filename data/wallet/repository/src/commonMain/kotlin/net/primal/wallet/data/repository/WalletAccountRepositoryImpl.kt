package net.primal.wallet.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.ActiveWalletData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.toDomain

class WalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
) : WalletAccountRepository {

    override suspend fun setActiveWallet(userId: String, walletId: String) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().upsertActiveWallet(data = ActiveWalletData(userId = userId, walletId = walletId))
        }

    override suspend fun clearActiveWallet(userId: String) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().clearActiveWallet(userId = userId)
        }

    override fun observeWalletsByUser(userId: String): Flow<List<Wallet>> =
        walletDatabase.wallet()
            .observeWalletsByUserId(userId = userId.asEncryptable())
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveWallet(userId: String): Wallet? =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().getActiveWallet(userId = userId)?.toDomain()
        }

    override fun observeActiveWallet(userId: String) =
        walletDatabase.wallet().observeActiveWallet(userId = userId)
            .distinctUntilChanged()
            .map { it?.toDomain() }

    override fun observeActiveWalletId(userId: String): Flow<String?> =
        walletDatabase.wallet().observeActiveWalletId(userId = userId)
            .distinctUntilChanged()

    override suspend fun findLastUsedWallet(userId: String, type: WalletType): Wallet? =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet()
                .findLastUsedWalletByType(userId = userId.asEncryptable(), type = type)
                ?.toDomain()
        }
}
