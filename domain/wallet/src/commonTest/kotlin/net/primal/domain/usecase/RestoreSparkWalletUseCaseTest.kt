package net.primal.domain.usecase

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class RestoreSparkWalletUseCaseTest {

    private val userId = "user123"
    private val newWalletId = "new_wallet_789"
    private val seedPhrase = "abandon ability able about above absent absorb abstract absurd abuse access accident"

    @Test
    fun happyPath_noExistingWallets_fullRestoreFlow() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(
                    walletId = newWalletId,
                    callLog = callOrder,
                ),
            )

            val result = useCase.invoke(seedWords = seedPhrase, userId = userId)

            result.getOrThrow() shouldBe newWalletId
            callOrder shouldBe listOf(
                "initializeWallet",
                "persistSeedWords",
                "registerSparkWallet",
                "fetchWalletAccountInfo",
                "markWalletAsBackedUp",
                "setActiveWallet",
                "findAllPersistedWalletIds",
            )
        }

    @Test
    fun initializeWalletFails_returnsFailure_noFurtherCalls() =
        runTest {
            val error = RuntimeException("init failed")
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(
                    initError = error,
                    callLog = callOrder,
                ),
            )

            val result = useCase.invoke(seedWords = seedPhrase, userId = userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
            callOrder.contains("persistSeedWords") shouldBe false
            callOrder.contains("registerSparkWallet") shouldBe false
            callOrder.contains("setActiveWallet") shouldBe false
        }

    @Test
    fun fetchWalletAccountInfoFails_fallsBackToEnsureWalletInfoExists() =
        runTest {
            val callOrder = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    fetchWalletAccountInfoError = RuntimeException("fetch failed"),
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(
                    walletId = newWalletId,
                    callLog = callOrder,
                ),
            )

            val result = useCase.invoke(seedWords = seedPhrase, userId = userId)

            result.getOrThrow() shouldBe newWalletId
            callOrder shouldBe listOf(
                "initializeWallet",
                "persistSeedWords",
                "registerSparkWallet",
                "fetchWalletAccountInfo",
                "ensureWalletInfoExists",
                "markWalletAsBackedUp",
                "setActiveWallet",
                "findAllPersistedWalletIds",
            )
        }

    @Test
    fun passesCorrectArgumentsToAllCalls() =
        runTest {
            var capturedPersistWalletId: String? = null
            var capturedPersistSeedWords: String? = null
            var capturedRegisterUserId: String? = null
            var capturedRegisterWalletId: String? = null
            var capturedActiveUserId: String? = null
            var capturedActiveWalletId: String? = null
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    onPersistSeedWords = { wid, sw ->
                        capturedPersistWalletId = wid
                        capturedPersistSeedWords = sw
                    },
                    onRegisterSparkWallet = { uid, wid ->
                        capturedRegisterUserId = uid
                        capturedRegisterWalletId = wid
                    },
                ),
                walletAccountRepository = FakeWalletAccountRepository(
                    onSetActiveWallet = { uid, wid ->
                        capturedActiveUserId = uid
                        capturedActiveWalletId = wid
                    },
                ),
                sparkWalletManager = FakeSparkWalletManager(walletId = newWalletId),
            )

            useCase.invoke(seedWords = seedPhrase, userId = userId)

            capturedPersistWalletId shouldBe newWalletId
            capturedPersistSeedWords shouldBe seedPhrase
            capturedRegisterUserId shouldBe userId
            capturedRegisterWalletId shouldBe newWalletId
            capturedActiveUserId shouldBe userId
            capturedActiveWalletId shouldBe newWalletId
        }

    @Test
    fun persistSeedWordsFails_returnsFailure_disconnectsNewWallet() =
        runTest {
            val error = RuntimeException("persist failed")
            val callOrder = mutableListOf<String>()
            var disconnectedWalletId: String? = null
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    persistSeedWordsError = error,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(
                    walletId = newWalletId,
                    callLog = callOrder,
                    onDisconnect = { disconnectedWalletId = it },
                ),
            )

            val result = useCase.invoke(seedWords = seedPhrase, userId = userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
            disconnectedWalletId shouldBe newWalletId
            callOrder.contains("registerSparkWallet") shouldBe false
            callOrder.contains("setActiveWallet") shouldBe false
        }

    @Test
    fun registerSparkWalletFails_returnsFailure_disconnectsNewWallet() =
        runTest {
            val error = RuntimeException("register failed")
            val callOrder = mutableListOf<String>()
            var disconnectedWalletId: String? = null
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    registerSparkWalletError = error,
                    callLog = callOrder,
                ),
                walletAccountRepository = FakeWalletAccountRepository(callLog = callOrder),
                sparkWalletManager = FakeSparkWalletManager(
                    walletId = newWalletId,
                    callLog = callOrder,
                    onDisconnect = { disconnectedWalletId = it },
                ),
            )

            val result = useCase.invoke(seedWords = seedPhrase, userId = userId)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
            disconnectedWalletId shouldBe newWalletId
            callOrder.contains("fetchWalletAccountInfo") shouldBe false
            callOrder.contains("setActiveWallet") shouldBe false
        }

    @Test
    fun existingWalletInstances_disconnectedAfterRestore() =
        runTest {
            val oldWalletA = "old_wallet_A"
            val oldWalletB = "old_wallet_B"
            val disconnected = mutableListOf<String>()
            val activeInstances = mutableSetOf(oldWalletA, oldWalletB)
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    allPersistedWalletIds = listOf(oldWalletA, oldWalletB, newWalletId),
                ),
                sparkWalletManager = FakeSparkWalletManager(
                    walletId = newWalletId,
                    activeInstances = activeInstances,
                    onDisconnect = { disconnected.add(it) },
                ),
            )

            val result = useCase.invoke(seedWords = seedPhrase, userId = userId)

            result.getOrThrow() shouldBe newWalletId
            disconnected.toSet() shouldBe setOf(oldWalletA, oldWalletB)
            (newWalletId in activeInstances) shouldBe true
            (oldWalletA in activeInstances) shouldBe false
            (oldWalletB in activeInstances) shouldBe false
        }

    @Test
    fun existingWalletInstances_newWalletNotDisconnected() =
        runTest {
            val disconnected = mutableListOf<String>()
            val useCase = buildUseCase(
                sparkWalletAccountRepository = FakeSparkWalletAccountRepository(
                    allPersistedWalletIds = listOf(newWalletId),
                ),
                sparkWalletManager = FakeSparkWalletManager(
                    walletId = newWalletId,
                    activeInstances = mutableSetOf(),
                    onDisconnect = { disconnected.add(it) },
                ),
            )

            val result = useCase.invoke(seedWords = seedPhrase, userId = userId)

            result.getOrThrow() shouldBe newWalletId
            disconnected shouldBe emptyList()
        }

    private fun buildUseCase(
        sparkWalletAccountRepository: FakeSparkWalletAccountRepository = FakeSparkWalletAccountRepository(),
        walletAccountRepository: FakeWalletAccountRepository = FakeWalletAccountRepository(),
        sparkWalletManager: FakeSparkWalletManager = FakeSparkWalletManager(walletId = newWalletId),
    ) = RestoreSparkWalletUseCase(
        sparkWalletManager = sparkWalletManager,
        walletAccountRepository = walletAccountRepository,
        sparkWalletAccountRepository = sparkWalletAccountRepository,
    )
}
