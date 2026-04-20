package net.primal.android.auth.repository

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.Credential
import net.primal.android.user.repository.BlossomRepository
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.core.testing.CoroutinesTestRule
import net.primal.core.testing.FakeDataStore
import net.primal.core.testing.FakeNostrNotary
import net.primal.core.utils.Result
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateAccountHandlerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private fun createAccountHandler(
        authRepository: AuthRepository = mockk(relaxed = true),
        relayRepository: RelayRepository = mockk(relaxed = true),
        userRepository: UserRepository = mockk(relaxed = true),
        blossomRepository: BlossomRepository = mockk(relaxed = true),
        settingsRepository: SettingsRepository = mockk(relaxed = true),
        credentialsStore: CredentialsStore = mockk(relaxed = true),
        eventsSignatureHandler: NostrEventSignatureHandler = FakeNostrNotary(
            expectedSignedNostrEvent = mockk(relaxed = true),
        ),
        ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase = mockk(relaxed = true) {
            coEvery { invoke(userId = any()) } returns Result.success("walletId")
        },
        sparkWalletAccountRepository: SparkWalletAccountRepository = mockk(relaxed = true),
    ): CreateAccountHandler {
        return CreateAccountHandler(
            authRepository = authRepository,
            relayRepository = relayRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository,
            credentialsStore = credentialsStore,
            dispatchers = coroutinesTestRule.dispatcherProvider,
            eventsSignatureHandler = eventsSignatureHandler,
            blossomRepository = blossomRepository,
            ensureSparkWalletExistsUseCase = ensureSparkWalletExistsUseCase,
            sparkWalletAccountRepository = sparkWalletAccountRepository,
        )
    }

    private fun createAuthRepository(
        credentialsStore: CredentialsStore = CredentialsStore(FakeDataStore(emptySet())),
        activeAccountStore: ActiveAccountStore = mockk(relaxed = true),
        userRepository: UserRepository = mockk(relaxed = true),
        accountsStore: UserAccountsStore = mockk(relaxed = true),
        connectionRepository: ConnectionRepository = mockk(relaxed = true),
    ) = AuthRepository(
        credentialsStore = credentialsStore,
        activeAccountStore = activeAccountStore,
        userRepository = userRepository,
        accountsStore = accountsStore,
        connectionRepository = connectionRepository,
    )

    private fun createDummyNostrEvent(
        userId: String,
        kind: Int = NostrEventKind.ApplicationSpecificData.value,
    ): NostrEvent =
        NostrEvent(
            id = "",
            pubKey = userId,
            createdAt = 0,
            kind = kind,
            content = "",
            sig = "",
        )

    @Test
    fun createNostrAccount_logsUserIn() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val authRepository = mockk<AuthRepository>(relaxed = true)
            val handler = createAccountHandler(
                authRepository = authRepository,
            )
            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
            )

            coVerify {
                authRepository.loginWithNsec(
                    withArg { it shouldBe keyPair.privateKey },
                )
            }
        }

    @Test
    fun createNostrAccount_setsGivenProfileMetadata() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val userRepository = mockk<UserRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                userRepository = userRepository,
                credentialsStore = credentialsStore,
            )

            val expectedProfileMetadata = ProfileMetadata(displayName = "Test", username = null)
            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = expectedProfileMetadata,
                followedUserIds = emptySet(),
            )

            coVerify {
                userRepository.setProfileMetadata(
                    withArg { it shouldBe keyPair.pubKey },
                    withArg { it shouldBe expectedProfileMetadata },
                )
            }
        }

    @Test
    fun createNostrAccount_alwaysFollowsSelf() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val userRepository = mockk<UserRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                authRepository = createAuthRepository(),
                userRepository = userRepository,
                credentialsStore = credentialsStore,
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
            )

            coVerify {
                userRepository.setFollowList(
                    withArg { it shouldBe keyPair.pubKey },
                    withArg { it shouldContain keyPair.pubKey },
                )
            }
        }

    @Test
    fun createNostrAccount_followsEveryFollowedProfileFromInterestsLists() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val userRepository = mockk<UserRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                userRepository = userRepository,
                credentialsStore = credentialsStore,
            )

            val followedUserIds = primalTeamMemberIds.filterIndexed { index, _ ->
                index % 2 == 0
            }.toSet()

            val expectedMemberIds = followedUserIds + keyPair.pubKey

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = followedUserIds,
            )

            coVerify {
                userRepository.setFollowList(
                    withArg { it shouldBe keyPair.pubKey },
                    withArg {
                        it shouldContainOnly expectedMemberIds
                    },
                )
            }
        }

    @Test
    fun createNostrAccount_bootstrapsTheDefaultRelays_withProperUserId() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val relayRepository = mockk<RelayRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val handler = createAccountHandler(
                relayRepository = relayRepository,
                credentialsStore = credentialsStore,
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
            )

            coVerify {
                relayRepository.bootstrapUserRelays(
                    withArg { it shouldBe keyPair.pubKey },
                    any(),
                )
            }
        }

    @Test
    fun createNostrAccount_fetchesAppSettings_withProperUserId() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val settingsRepository = mockk<SettingsRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }

            val expectedNostrEvent = createDummyNostrEvent(userId = keyPair.pubKey)

            val handler = createAccountHandler(
                settingsRepository = settingsRepository,
                credentialsStore = credentialsStore,
                eventsSignatureHandler = FakeNostrNotary(expectedSignedNostrEvent = expectedNostrEvent),
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
            )

            coVerify {
                settingsRepository.fetchAndPersistAppSettings(
                    withArg { it.pubKey shouldBe keyPair.pubKey },
                )
            }
        }

    @Test
    fun createNostrAccount_callsEnsureSparkWalletExistsUseCase_withSetAsActiveTrue() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }
            val ensureSparkWalletExistsUseCase = mockk<EnsureSparkWalletExistsUseCase>(relaxed = true) {
                coEvery { invoke(userId = any()) } returns Result.success("walletId")
            }
            val handler = createAccountHandler(
                credentialsStore = credentialsStore,
                ensureSparkWalletExistsUseCase = ensureSparkWalletExistsUseCase,
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
            )

            coVerify {
                ensureSparkWalletExistsUseCase.invoke(keyPair.pubKey)
            }
        }

    @Test
    fun createNostrAccount_revertsAuthData_ifAnyOfApiCallsFail() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val credentialsPersistence = FakeDataStore(emptySet<Credential>())
            val credentialsStore = CredentialsStore(persistence = credentialsPersistence)

            val activeAccountPersistence = FakeDataStore(initialValue = "")
            val activeAccountStore = ActiveAccountStore(
                dispatchers = coroutinesTestRule.dispatcherProvider,
                accountsStore = mockk(relaxed = true),
                persistence = activeAccountPersistence,
            )

            val authRepository = createAuthRepository(
                credentialsStore = credentialsStore,
                activeAccountStore = activeAccountStore,
            )
            val userRepository = mockk<UserRepository>(relaxed = true) {
                coEvery { setFollowList(any(), any()) } throws NetworkException()
            }
            val handler = createAccountHandler(
                authRepository = authRepository,
                userRepository = userRepository,
            )

            try {
                handler.createNostrAccount(
                    privateKey = keyPair.privateKey,
                    profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                    followedUserIds = emptySet(),
                )
            } catch (_: CreateAccountHandler.AccountCreationException) {
            }

            credentialsPersistence.latestData shouldBe emptyList()
            activeAccountPersistence.latestData shouldBe ""
        }

    @Test
    fun createNostrAccount_usesPreFetchedRelays() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val relayRepository = mockk<RelayRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }
            val preFetchedRelays = listOf("wss://relay.primal.net", "wss://relay.damus.io")

            val handler = createAccountHandler(
                relayRepository = relayRepository,
                credentialsStore = credentialsStore,
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
                preFetchedRelays = preFetchedRelays,
            )

            coVerify {
                relayRepository.bootstrapUserRelays(
                    withArg { it shouldBe keyPair.pubKey },
                    withArg { relays -> relays shouldBe preFetchedRelays },
                )
            }
        }

    @Test
    fun createNostrAccount_succeedsEvenWhenWalletFails() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val authRepository = mockk<AuthRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }
            val ensureSparkWalletExistsUseCase = mockk<EnsureSparkWalletExistsUseCase>(relaxed = true) {
                coEvery { invoke(userId = any()) } returns Result.failure(RuntimeException("Wallet init failed"))
            }
            val handler = createAccountHandler(
                authRepository = authRepository,
                credentialsStore = credentialsStore,
                ensureSparkWalletExistsUseCase = ensureSparkWalletExistsUseCase,
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
            )
            advanceUntilIdle()

            coVerify {
                ensureSparkWalletExistsUseCase.invoke(userId = keyPair.pubKey)
                authRepository.loginWithNsec(withArg { it shouldBe keyPair.privateKey })
            }
        }

    @Test
    fun createNostrAccount_succeedsEvenWhenSettingsFails() =
        runTest {
            val keyPair = CryptoUtils.generateHexEncodedKeypair()
            val authRepository = mockk<AuthRepository>(relaxed = true)
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns keyPair.pubKey
            }
            val settingsRepository = mockk<SettingsRepository>(relaxed = true) {
                coEvery { fetchAndPersistAppSettings(any()) } throws NetworkException()
            }
            val handler = createAccountHandler(
                authRepository = authRepository,
                credentialsStore = credentialsStore,
                settingsRepository = settingsRepository,
            )

            handler.createNostrAccount(
                privateKey = keyPair.privateKey,
                profileMetadata = ProfileMetadata(displayName = "Test", username = null),
                followedUserIds = emptySet(),
            )
            advanceUntilIdle()

            coVerify {
                settingsRepository.fetchAndPersistAppSettings(any())
                authRepository.loginWithNsec(withArg { it shouldBe keyPair.privateKey })
            }
        }

    private val primalTeamMemberIds = listOf(
        "npub1ky9s6hjl46wxcj9gcalhuk4ag2nea9yqufdyp9q9r496fns5g44sw0alex",
        "npub19f2765hdx8u9lz777w7azed2wsn9mqkf2gvn67mkldx8dnxvggcsmhe9da",
        "npub1k6tqlj78cpznd0yc74wy3k0elmj4nql87a3uzfz98tmj3tuzxywsf0dhk6",
        "npub1mkde3807tcyyp2f98re7n7z0va8979atqkfja7avknvwdjg97vpq6ef0jp",
        "npub1zga04e73s7ard4kaektaha9vckdwll3y8auztyhl3uj764ua7vrqc7ppvc",
        "npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr",
    )
}
