package net.primal.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.primal.core.utils.Result
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.UnclaimedDepositEvent
import net.primal.domain.wallet.UserWallet
import net.primal.domain.wallet.WalletType

internal class FakeSparkWalletManager(
    private val walletId: String = "",
    private val initError: Throwable? = null,
    private val activeInstances: MutableSet<String> = mutableSetOf(),
    private val callLog: MutableList<String>? = null,
    private val onInitialize: ((String) -> Unit)? = null,
    private val onDisconnect: ((String) -> Unit)? = null,
) : SparkWalletManager {
    override val unclaimedDeposits: Flow<UnclaimedDepositEvent> = emptyFlow()
    override val balanceChanged: Flow<String> = emptyFlow()

    override suspend fun initializeWallet(seedWords: String): Result<String> {
        callLog?.add("initializeWallet")
        onInitialize?.invoke(seedWords)
        if (initError != null) return Result.failure(initError)
        activeInstances.add(walletId)
        return Result.success(walletId)
    }

    override suspend fun disconnectWallet(walletId: String): Result<Unit> {
        callLog?.add("disconnectWallet")
        onDisconnect?.invoke(walletId)
        activeInstances.remove(walletId)
        return Result.success(Unit)
    }

    override suspend fun hasInstance(walletId: String): Boolean = walletId in activeInstances
}

@Suppress("LongParameterList")
internal class FakeSparkWalletAccountRepository(
    private val registeredWalletId: String? = null,
    private val allPersistedWalletIds: List<String> = emptyList(),
    private val persistedSeedWords: List<String> = emptyList(),
    private val isRegistered: Boolean = false,
    private val persistSeedWordsError: Throwable? = null,
    private val registerSparkWalletError: Throwable? = null,
    private val fetchWalletAccountInfoError: Throwable? = null,
    private val getPersistedSeedWordsError: Throwable? = null,
    private val callLog: MutableList<String>? = null,
    private val onPersistSeedWords: ((String, String) -> Unit)? = null,
    private val onGetPersistedSeedWords: ((String) -> Unit)? = null,
    private val onRegisterSparkWallet: ((String, String) -> Unit)? = null,
) : SparkWalletAccountRepository {

    override suspend fun hasPersistedSparkWallet(userId: String): Boolean {
        callLog?.add("hasPersistedSparkWallet")
        return registeredWalletId != null || allPersistedWalletIds.isNotEmpty()
    }

    override suspend fun findRegisteredSparkWalletId(userId: String): String? {
        callLog?.add("findRegisteredSparkWalletId")
        return registeredWalletId
    }

    override suspend fun getPersistedSeedWords(walletId: String): Result<List<String>> {
        callLog?.add("getPersistedSeedWords")
        onGetPersistedSeedWords?.invoke(walletId)
        return if (getPersistedSeedWordsError != null) {
            Result.failure(getPersistedSeedWordsError)
        } else {
            Result.success(persistedSeedWords)
        }
    }

    override suspend fun persistSeedWords(walletId: String, seedWords: String): Result<Unit> {
        callLog?.add("persistSeedWords")
        onPersistSeedWords?.invoke(walletId, seedWords)
        return if (persistSeedWordsError != null) Result.failure(persistSeedWordsError) else Result.success(Unit)
    }

    override suspend fun registerSparkWallet(userId: String, walletId: String): Result<Unit> {
        callLog?.add("registerSparkWallet")
        onRegisterSparkWallet?.invoke(userId, walletId)
        return if (registerSparkWalletError != null) {
            Result.failure(registerSparkWalletError)
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun fetchWalletAccountInfo(userId: String, walletId: String): Result<Unit> {
        callLog?.add("fetchWalletAccountInfo")
        return if (fetchWalletAccountInfoError != null) {
            Result.failure(fetchWalletAccountInfoError)
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun isRegistered(userId: String, walletId: String): Boolean {
        callLog?.add("isRegistered")
        return isRegistered
    }

    override suspend fun isWalletBackedUp(walletId: String): Boolean = false
    override suspend fun markWalletAsBackedUp(walletId: String): Result<Unit> {
        callLog?.add("markWalletAsBackedUp")
        return Result.success(Unit)
    }
    override suspend fun unregisterSparkWallet(userId: String, walletId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getLightningAddress(userId: String, walletId: String): String? = null
    override suspend fun findAllPersistedWalletIds(userId: String): List<String> {
        callLog?.add("findAllPersistedWalletIds")
        return allPersistedWalletIds
    }
    override suspend fun isPrimalTxsMigrationCompleted(walletId: String): Boolean = true
    override suspend fun ensureWalletInfoExists(userId: String, walletId: String) {
        callLog?.add("ensureWalletInfoExists")
    }
}

internal class FakeWalletAccountRepository(
    private val activeWallet: UserWallet? = null,
    private val callLog: MutableList<String>? = null,
    private val onSetActiveWallet: ((String, String) -> Unit)? = null,
) : WalletAccountRepository {
    override suspend fun setActiveWallet(userId: String, walletId: String) {
        callLog?.add("setActiveWallet")
        onSetActiveWallet?.invoke(userId, walletId)
    }

    override suspend fun clearActiveWallet(userId: String) = Unit
    override fun observeWalletsByUser(userId: String): Flow<List<UserWallet>> = emptyFlow()
    override suspend fun findLastUsedWallet(userId: String, type: WalletType): UserWallet? = null
    override suspend fun findLastUsedWallet(userId: String, type: Set<WalletType>): UserWallet? = null
    override suspend fun getActiveWallet(userId: String): UserWallet? = activeWallet
    override fun observeActiveWallet(userId: String): Flow<UserWallet?> = emptyFlow()
    override fun observeActiveWalletId(userId: String): Flow<String?> = emptyFlow()
}
