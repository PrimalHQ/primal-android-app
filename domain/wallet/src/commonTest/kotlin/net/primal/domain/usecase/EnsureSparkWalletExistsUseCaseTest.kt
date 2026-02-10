package net.primal.domain.usecase

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import net.primal.core.utils.Result
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SeedPhraseGenerator
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.UnclaimedDepositEvent
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletType

class EnsureSparkWalletExistsUseCaseTest {

    private val userId = "user123"
    private val walletId = "wallet456"
    private val seedWords = listOf(
        "abandon", "ability", "able", "about", "above", "absent",
        "absorb", "abstract", "absurd", "abuse", "access", "accident",
    )
    private val seedPhrase = seedWords.joinToString(separator = " ")

    @Test
    fun newWallet_generatesSeeds_initializesWallet_persistsSeeds_registersLnAddress_fetchesInfo_setsActive() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = null,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.getOrThrow() shouldBe walletId
            callOrder shouldBe listOf(
                "findPersistedWalletId",
                "initializeWallet",
                "persistSeedWords",
                "isRegistered",
                "registerSparkWallet",
                "fetchWalletAccountInfo",
                "setActiveWallet",
            )
        }

    @Test
    fun existingWallet_restoresSeeds_initializesWallet_fetchesInfo_skipsRegistration() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = walletId,
                    persistedSeedWords = seedWords,
                    isRegistered = true,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.getOrThrow() shouldBe walletId
            callOrder shouldBe listOf(
                "findPersistedWalletId",
                "getPersistedSeedWords",
                "initializeWallet",
                "isRegistered",
                "fetchWalletAccountInfo",
                "setActiveWallet",
            )
        }

    @Test
    fun existingWallet_missingLnAddress_registersLightningAddress() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = walletId,
                    persistedSeedWords = seedWords,
                    isRegistered = false,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.getOrThrow() shouldBe walletId
            callOrder shouldBe listOf(
                "findPersistedWalletId",
                "getPersistedSeedWords",
                "initializeWallet",
                "isRegistered",
                "registerSparkWallet",
                "fetchWalletAccountInfo",
                "setActiveWallet",
            )
        }

    @Test
    fun registerFalse_skipsRegistrationButStillFetchesInfo() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = null,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId, register = false)

            result.getOrThrow() shouldBe walletId
            callOrder shouldBe listOf(
                "findPersistedWalletId",
                "initializeWallet",
                "persistSeedWords",
                "fetchWalletAccountInfo",
                "setActiveWallet",
            )
            // Should NOT contain registration-related calls
            callOrder.contains("isRegistered") shouldBe false
            callOrder.contains("registerSparkWallet") shouldBe false
        }

    @Test
    fun seedGenerationFails_returnsFailure() =
        runTest {
            val error = RuntimeException("seed generation failed")
            val useCase = buildUseCase(
                seedPhraseGenerator = FakeSeedPhraseGenerator(error = error),
            )

            val result = useCase.invoke(userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }

    @Test
    fun walletInitializationFails_returnsFailure() =
        runTest {
            val error = RuntimeException("wallet init failed")
            val useCase = buildUseCase(
                sparkWalletManager = FakeSparkWalletManager(error = error),
            )

            val result = useCase.invoke(userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }

    @Test
    fun persistSeedWordsFails_returnsFailure_doesNotRegisterOrActivate() =
        runTest {
            val error = RuntimeException("persist failed")
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = null,
                    persistSeedWordsError = error,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
            callOrder.contains("registerSparkWallet") shouldBe false
            callOrder.contains("setActiveWallet") shouldBe false
        }

    @Test
    fun registerSparkWalletFails_returnsFailure_doesNotFetchOrActivate() =
        runTest {
            val error = RuntimeException("register failed")
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = null,
                    registerSparkWalletError = error,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
            callOrder.contains("fetchWalletAccountInfo") shouldBe false
            callOrder.contains("setActiveWallet") shouldBe false
        }

    @Test
    fun fetchWalletAccountInfoFails_returnsFailure_doesNotActivate() =
        runTest {
            val error = RuntimeException("fetch failed")
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = null,
                    fetchWalletAccountInfoError = error,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
            callOrder.contains("setActiveWallet") shouldBe false
        }

    @Test
    fun existingWallet_fetchFails_returnsFailure() =
        runTest {
            val error = RuntimeException("fetch failed")
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = walletId,
                    persistedSeedWords = seedWords,
                    fetchWalletAccountInfoError = error,
                ),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }

    @Test
    fun existingWallet_getPersistedSeedWordsFails_returnsFailure() =
        runTest {
            val error = RuntimeException("seed words not found")
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = walletId,
                    getPersistedSeedWordsError = error,
                ),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }

    @Test
    fun newWallet_passesCorrectSeedPhraseToWalletManager() =
        runTest {
            var capturedSeedPhrase: String? = null
            val useCase = buildUseCase(
                sparkWalletManager = FakeSparkWalletManager(
                    walletId = walletId,
                    onInitialize = { capturedSeedPhrase = it },
                ),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            useCase.invoke(userId)

            capturedSeedPhrase shouldBe seedPhrase
        }

    @Test
    fun newWallet_passesCorrectArgumentsToPersistSeedWords() =
        runTest {
            var capturedUserId: String? = null
            var capturedWalletId: String? = null
            var capturedSeedWords: String? = null
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistedWalletId = null,
                    onPersistSeedWords = { uid, wid, sw ->
                        capturedUserId = uid
                        capturedWalletId = wid
                        capturedSeedWords = sw
                    },
                ),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            useCase.invoke(userId)

            capturedUserId shouldBe userId
            capturedWalletId shouldBe walletId
            capturedSeedWords shouldBe seedPhrase
        }

    private fun buildUseCase(
        sparkWalletAccountRepository: SparkWalletAccountRepository = FakeSparkWalletAccountRepository(),
        walletAccountRepository: WalletAccountRepository = FakeWalletAccountRepository(),
        sparkWalletManager: SparkWalletManager = FakeSparkWalletManager(walletId = walletId),
        seedPhraseGenerator: SeedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
    ) = EnsureSparkWalletExistsUseCase(
        sparkWalletManager = sparkWalletManager,
        sparkWalletAccountRepository = sparkWalletAccountRepository,
        walletAccountRepository = walletAccountRepository,
        seedPhraseGenerator = seedPhraseGenerator,
    )
}

private class FakeSeedPhraseGenerator(
    private val seedWords: List<String> = emptyList(),
    private val error: Throwable? = null,
) : SeedPhraseGenerator {
    override fun generate(wordCount: Int): Result<List<String>> =
        if (error != null) Result.failure(error) else Result.success(seedWords)
}

private class FakeSparkWalletManager(
    private val walletId: String = "",
    private val error: Throwable? = null,
    private val callLog: MutableList<String>? = null,
    private val onInitialize: ((String) -> Unit)? = null,
) : SparkWalletManager {
    override val unclaimedDeposits: Flow<UnclaimedDepositEvent> = emptyFlow()
    override val balanceChanged: Flow<String> = emptyFlow()

    override suspend fun initializeWallet(seedWords: String): Result<String> {
        callLog?.add("initializeWallet")
        onInitialize?.invoke(seedWords)
        return if (error != null) Result.failure(error) else Result.success(walletId)
    }

    override suspend fun disconnectWallet(walletId: String): Result<Unit> = Result.success(Unit)
}

@Suppress("LongParameterList")
private class FakeSparkWalletAccountRepository(
    private val persistedWalletId: String? = null,
    private val persistedSeedWords: List<String> = emptyList(),
    private val isRegistered: Boolean = false,
    private val persistSeedWordsError: Throwable? = null,
    private val registerSparkWalletError: Throwable? = null,
    private val fetchWalletAccountInfoError: Throwable? = null,
    private val getPersistedSeedWordsError: Throwable? = null,
    private val callLog: MutableList<String>? = null,
    private val onPersistSeedWords: ((String, String, String) -> Unit)? = null,
) : SparkWalletAccountRepository {

    override suspend fun findPersistedWalletId(userId: String): String? {
        callLog?.add("findPersistedWalletId")
        return persistedWalletId
    }

    override suspend fun getPersistedSeedWords(walletId: String): Result<List<String>> {
        callLog?.add("getPersistedSeedWords")
        return if (getPersistedSeedWordsError != null) {
            Result.failure(getPersistedSeedWordsError)
        } else {
            Result.success(persistedSeedWords)
        }
    }

    override suspend fun persistSeedWords(
        userId: String,
        walletId: String,
        seedWords: String,
    ): Result<Unit> {
        callLog?.add("persistSeedWords")
        onPersistSeedWords?.invoke(userId, walletId, seedWords)
        return if (persistSeedWordsError != null) Result.failure(persistSeedWordsError) else Result.success(Unit)
    }

    override suspend fun registerSparkWallet(userId: String, walletId: String): Result<Unit> {
        callLog?.add("registerSparkWallet")
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

    override suspend fun isRegistered(walletId: String): Boolean {
        callLog?.add("isRegistered")
        return isRegistered
    }

    override suspend fun isWalletBackedUp(walletId: String): Boolean = false
    override suspend fun markWalletAsBackedUp(walletId: String): Result<Unit> = Result.success(Unit)
    override suspend fun deleteSparkWalletByUserId(userId: String): Result<String> = Result.success("")
    override suspend fun unregisterSparkWallet(userId: String, walletId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getLightningAddress(walletId: String): String? = null
    override suspend fun isPrimalTxsMigrationCompleted(walletId: String): Boolean = true
    override suspend fun clearPrimalTxsMigrationState(walletId: String) = Unit
}

private class FakeWalletAccountRepository(
    private val callLog: MutableList<String>? = null,
) : WalletAccountRepository {
    override suspend fun setActiveWallet(userId: String, walletId: String) {
        callLog?.add("setActiveWallet")
    }

    override suspend fun clearActiveWallet(userId: String) = Unit
    override fun observeWalletsByUser(userId: String): Flow<List<Wallet>> = emptyFlow()
    override suspend fun findLastUsedWallet(userId: String, type: WalletType): Wallet? = null
    override suspend fun findLastUsedWallet(userId: String, type: Set<WalletType>): Wallet? = null
    override suspend fun getActiveWallet(userId: String): Wallet? = null
    override fun observeActiveWallet(userId: String): Flow<Wallet?> = emptyFlow()
    override fun observeActiveWalletId(userId: String): Flow<String?> = emptyFlow()
}
