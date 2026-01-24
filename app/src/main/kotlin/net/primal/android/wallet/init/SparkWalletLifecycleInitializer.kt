package net.primal.android.wallet.init

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.wallet.data.generator.RecoveryPhraseGenerator
import timber.log.Timber

@Singleton
class SparkWalletLifecycleInitializer @Inject constructor(
    dispatchers: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
) {

    private val scope = CoroutineScope(dispatchers.io())

    private val walletMutex = Mutex()
    private var currentWalletId: String? = null

    private val recoveryPhraseGenerator = RecoveryPhraseGenerator()

    fun start() {
        scope.launch {
            activeAccountStore.activeUserId
                .map { it.takeIf { id -> id.isNotBlank() } }
                .distinctUntilChanged()
                .collect { userIdOrNull ->
                    walletMutex.withLock {
                        // Disconnect current wallet if exists
                        currentWalletId?.let { walletId ->
                            runCatching {
                                sparkWalletAccountRepository.disconnectWallet(walletId).getOrThrow()
                            }.onFailure { t ->
                                Timber.e(t, "terminateWallet failed for walletId=%s", walletId)
                            }
                            currentWalletId = null
                        }

                        // Initialize wallet for new user
                        val userId = userIdOrNull ?: return@withLock
                        initializeWalletForUser(userId)
                    }
                }
        }
    }

    private suspend fun initializeWalletForUser(userId: String) {
        val allPersistedSeedWords = sparkWalletAccountRepository.getPersistedSeedWords(userId)
        val persistedSeedWords = allPersistedSeedWords.firstOrNull()
        val isNewWallet = persistedSeedWords == null

        val seedWords = if (persistedSeedWords != null) {
            persistedSeedWords
        } else {
            val generationResult = recoveryPhraseGenerator.generate(wordCount = 12)
            if (generationResult.isFailure) {
                Timber.e(
                    generationResult.exceptionOrNull(),
                    "Failed to generate recovery phrase for userId=%s",
                    userId,
                )
                return
            }
            generationResult.getOrThrow().joinToString(separator = " ")
        }

        runCatching {
            sparkWalletAccountRepository.initializeWallet(
                userId = userId,
                seedWords = seedWords,
            ).getOrThrow()
        }.onSuccess { walletId ->
            currentWalletId = walletId

            if (isNewWallet) {
                sparkWalletAccountRepository.persistSeedWords(userId, walletId, seedWords)
                    .onFailure { t ->
                        Timber.e(t, "Failed to persist seed words for userId=%s, walletId=%s", userId, walletId)
                    }

                sparkWalletAccountRepository.fetchWalletAccountInfo(userId, walletId)
                    .onFailure { t ->
                        Timber.w(t, "fetchWalletAccountInfo failed for userId=%s, walletId=%s", userId, walletId)
                    }
            }
        }.onFailure { t ->
            Timber.e(t, "initializeWallet failed for userId=%s", userId)
        }
    }
}
