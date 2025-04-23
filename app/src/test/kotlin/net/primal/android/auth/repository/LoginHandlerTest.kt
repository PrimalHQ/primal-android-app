package net.primal.android.auth.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.core.FakeDataStore
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.Credential
import net.primal.android.user.domain.LoginType
import net.primal.android.user.repository.UserRepository
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.SignResult
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginHandlerTest {

    private val expectedUserId by lazy {
        "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079"
    }

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private fun createLoginHandler(
        settingsRepository: SettingsRepository = mockk(relaxed = true),
        authRepository: AuthRepository = mockk(relaxed = true),
        userRepository: UserRepository = mockk(relaxed = true),
        mutedItemRepository: MutedItemRepository = mockk(relaxed = true),
        bookmarksRepository: PublicBookmarksRepository = mockk(relaxed = true),
        credentialsStore: CredentialsStore = mockk(relaxed = true),
        nostrNotary: NostrNotary = mockk(relaxed = true),
    ): LoginHandler =
        LoginHandler(
            settingsRepository = settingsRepository,
            authRepository = authRepository,
            userRepository = userRepository,
            mutedItemRepository = mutedItemRepository,
            bookmarksRepository = bookmarksRepository,
            dispatchers = coroutinesTestRule.dispatcherProvider,
            credentialsStore = credentialsStore,
            nostrNotary = nostrNotary,
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
    fun login_callsLoginFromAuthRepository_withGivenKey() =
        runTest {
            val expectedKey = "random"
            val authRepository = mockk<AuthRepository>(relaxed = true)
            val loginHandler = createLoginHandler(authRepository = authRepository)
            loginHandler.login(
                nostrKey = expectedKey,
                loginType = LoginType.PrivateKey,
                authorizationEvent = null,
            )

            coVerify {
                authRepository.loginWithNsec(nostrKey = expectedKey)
            }
        }

    @Test
    fun login_callsFetchAndUpdateUserAccount() =
        runTest {
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns expectedUserId
            }
            val userRepository = mockk<UserRepository>(relaxed = true)
            val loginHandler = createLoginHandler(
                userRepository = userRepository,
                credentialsStore = credentialsStore,
            )
            loginHandler.login(
                nostrKey = "random",
                loginType = LoginType.PrivateKey,
                authorizationEvent = null,
            )

            coVerify {
                userRepository.fetchAndUpdateUserAccount(expectedUserId)
            }
        }

    @Test
    fun login_callsFetchAndPersistAppSettings() =
        runTest {
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns expectedUserId
            }
            val settingsRepository = mockk<SettingsRepository>(relaxed = true)
            val expectedNostrEvent = createDummyNostrEvent(userId = expectedUserId)

            val loginHandler = createLoginHandler(
                settingsRepository = settingsRepository,
                credentialsStore = credentialsStore,
                nostrNotary = mockk(relaxed = true) {
                    coEvery {
                        signAuthorizationNostrEvent(expectedUserId, any(), any())
                    } returns SignResult.Signed(expectedNostrEvent)
                },
            )

            loginHandler.login(
                nostrKey = "nsec174p4ny7",
                loginType = LoginType.PrivateKey,
                authorizationEvent = null,
            )
            advanceUntilIdle()

            coVerify {
                settingsRepository.fetchAndPersistAppSettings(
                    withArg { it.pubKey shouldBe expectedUserId },
                )
            }
        }

    @Test
    fun login_callsFetchAndPersistMuteList() =
        runTest {
            val credentialsStore = mockk<CredentialsStore>(relaxed = true) {
                coEvery { saveNsec(any()) } returns expectedUserId
            }
            val mutedItemRepository = mockk<MutedItemRepository>(relaxed = true)
            val loginHandler = createLoginHandler(
                mutedItemRepository = mutedItemRepository,
                credentialsStore = credentialsStore,
            )
            loginHandler.login(
                nostrKey = "random",
                loginType = LoginType.PrivateKey,
                authorizationEvent = null,
            )

            coVerify {
                mutedItemRepository.fetchAndPersistMuteList(expectedUserId)
            }
        }

    @Test
    fun login_revertsLoginData_ifAnyOfApiCallsFail() =
        runTest {
            val nsec = "nsec1p64ty2pgcj6k2c6v7u9dwu7aesle8v9qelnpgx4zrfa37av8f24qyftvle"
            val credentialsPersistence = FakeDataStore(emptySet<Credential>())
            val credentialsStore = CredentialsStore(persistence = credentialsPersistence)

            val activeAccountPersistence = FakeDataStore(initialValue = "")
            val activeAccountStore = ActiveAccountStore(
                dispatchers = coroutinesTestRule.dispatcherProvider,
                accountsStore = mockk(relaxed = true),
                persistence = activeAccountPersistence,
            )

            val authRepository = AuthRepository(
                credentialsStore = credentialsStore,
                activeAccountStore = activeAccountStore,
                userRepository = mockk(relaxed = true),
                accountsStore = mockk(relaxed = true),
            )

            val userRepository = mockk<UserRepository>(relaxed = true) {
                coEvery { fetchAndUpdateUserAccount(any()) } throws NetworkException()
            }
            val loginHandler = createLoginHandler(
                authRepository = authRepository,
                userRepository = userRepository,
            )

            try {
                loginHandler.login(
                    nostrKey = nsec,
                    loginType = LoginType.PrivateKey,
                    authorizationEvent = null,
                )
            } catch (_: NetworkException) {
            }

            credentialsPersistence.latestData shouldBe emptyList()
            activeAccountPersistence.latestData shouldBe ""
        }
}
