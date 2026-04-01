package net.primal.domain.usecase

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import net.primal.core.utils.Result
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SeedPhraseGenerator
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.UserWallet
import net.primal.domain.wallet.Wallet

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
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.getOrThrow() shouldBe walletId
            callOrder shouldBe listOf(
                "findRegisteredSparkWalletId",
                "findAllPersistedWalletIds",
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
                    registeredWalletId = walletId,
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
                "findRegisteredSparkWalletId",
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
                    registeredWalletId = walletId,
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
                "findRegisteredSparkWalletId",
                "getPersistedSeedWords",
                "initializeWallet",
                "isRegistered",
                "registerSparkWallet",
                "fetchWalletAccountInfo",
                "setActiveWallet",
            )
        }

    @Test
    fun noRegisteredWallet_unregisteredExists_fallsBackToUnregistered() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    registeredWalletId = null,
                    allPersistedWalletIds = listOf(walletId),
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
                "findRegisteredSparkWalletId",
                "findAllPersistedWalletIds",
                "getPersistedSeedWords",
                "initializeWallet",
                "isRegistered",
                "registerSparkWallet",
                "fetchWalletAccountInfo",
                "setActiveWallet",
            )
        }

    @Test
    fun noRegisteredWallet_multipleUnregistered_usesFirst() =
        runTest {
            val otherWalletId = "other_wallet"
            val callOrder = mutableListOf<String>()
            var capturedGetSeedWordsWalletId: String? = null
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    registeredWalletId = null,
                    allPersistedWalletIds = listOf(walletId, otherWalletId),
                    persistedSeedWords = seedWords,
                    isRegistered = false,
                    callLog = callOrder,
                    onGetPersistedSeedWords = { capturedGetSeedWordsWalletId = it },
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.getOrThrow() shouldBe walletId
            capturedGetSeedWordsWalletId shouldBe walletId
        }

    @Test
    fun registeredWalletExists_withUnregistered_usesRegistered_skipsFallback() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    registeredWalletId = walletId,
                    allPersistedWalletIds = listOf("other1", "other2"),
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
                "findRegisteredSparkWalletId",
                "getPersistedSeedWords",
                "initializeWallet",
                "isRegistered",
                "fetchWalletAccountInfo",
                "setActiveWallet",
            )
            callOrder.contains("findAllPersistedWalletIds") shouldBe false
        }

    @Test
    fun registerFalse_skipsRegistrationButStillFetchesInfo() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId, register = false)

            result.getOrThrow() shouldBe walletId
            callOrder shouldBe listOf(
                "findRegisteredSparkWalletId",
                "findAllPersistedWalletIds",
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
                sparkWalletManager = FakeSparkWalletManager(initError = error),
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
                    registeredWalletId = walletId,
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
                    registeredWalletId = walletId,
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
            var capturedWalletId: String? = null
            var capturedSeedWords: String? = null
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    onPersistSeedWords = { wid, sw ->
                        capturedWalletId = wid
                        capturedSeedWords = sw
                    },
                ),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            useCase.invoke(userId)

            capturedWalletId shouldBe walletId
            capturedSeedWords shouldBe seedPhrase
        }

    @Test
    fun activeWalletAlreadyExists_skipsSetActiveWallet() =
        runTest {
            val callOrder = mutableListOf<String>()
            val existingActiveWallet = UserWallet(
                userId = userId,
                wallet = Wallet.Spark(
                    walletId = "existing_wallet",
                    spamThresholdAmountInSats = 1L,
                    balanceInBtc = null,
                    maxBalanceInBtc = null,
                    lastUpdatedAt = null,
                    isBackedUp = false,
                ),
                lightningAddress = null,
            )
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(
                    activeWallet = existingActiveWallet,
                    callLog = callOrder,
                ),
                sparkWalletManager = FakeSparkWalletManager(walletId = walletId, callLog = callOrder),
                seedPhraseGenerator = FakeSeedPhraseGenerator(seedWords = seedWords),
            )

            val result = useCase.invoke(userId)

            result.getOrThrow() shouldBe walletId
            callOrder.contains("setActiveWallet") shouldBe false
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
