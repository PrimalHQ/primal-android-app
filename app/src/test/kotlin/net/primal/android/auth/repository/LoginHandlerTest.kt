package net.primal.android.auth.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.primal.android.feed.db.Feed
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.test.FakeDataStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.Credential
import net.primal.android.user.repository.UserRepository
import org.junit.Test

class LoginHandlerTest {

    private fun createLoginHandler(
        settingsRepository: SettingsRepository = mockk(relaxed = true),
        authRepository: AuthRepository = mockk(relaxed = true),
        userRepository: UserRepository = mockk(relaxed = true),
        mutedUserRepository: MutedUserRepository = mockk(relaxed = true),
        feedRepository: FeedRepository = mockk(relaxed = true),
    ): LoginHandler = LoginHandler(
        settingsRepository = settingsRepository,
        authRepository = authRepository,
        userRepository = userRepository,
        mutedUserRepository = mutedUserRepository,
        feedRepository = feedRepository,
    )

    @Test
    fun login_returnsDefaultFeed() = runTest {
        val expectedFeedDirective = "directive"
        val feedRepository = mockk<FeedRepository>(relaxed = true) {
            every { defaultFeed() } returns Feed(directive = expectedFeedDirective, name = "Latest")
        }

        val loginHandler = createLoginHandler(feedRepository = feedRepository)
        val actualFeedDirective = loginHandler.loginAndReturnDefaultFeed(nostrKey = "random")
        actualFeedDirective shouldBe expectedFeedDirective
    }

    @Test
    fun login_callsLoginFromAuthRepository_withGivenKey() = runTest {
        val expectedKey = "random"
        val authRepository = mockk<AuthRepository>(relaxed = true)
        val loginHandler = createLoginHandler(authRepository = authRepository)
        loginHandler.loginAndReturnDefaultFeed(nostrKey = expectedKey)

        coVerify {
            authRepository.login(nostrKey = expectedKey)
        }
    }

    @Test
    fun login_callsFetchAndUpdateUserAccount() = runTest {
        val expectedUserId = "b10a23"
        val authRepository = mockk<AuthRepository>(relaxed = true) {
            coEvery { login(any()) } returns expectedUserId
        }
        val userRepository = mockk<UserRepository>(relaxed = true)
        val loginHandler = createLoginHandler(
            authRepository = authRepository,
            userRepository = userRepository,
        )
        loginHandler.loginAndReturnDefaultFeed(nostrKey = "random")

        coVerify {
            userRepository.fetchAndUpdateUserAccount(expectedUserId)
        }
    }

    @Test
    fun login_callsFetchAndPersistAppSettings() = runTest {
        val expectedUserId = "b10a23"
        val authRepository = mockk<AuthRepository>(relaxed = true) {
            coEvery { login(any()) } returns expectedUserId
        }
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)
        val loginHandler = createLoginHandler(
            authRepository = authRepository,
            settingsRepository = settingsRepository,
        )
        loginHandler.loginAndReturnDefaultFeed(nostrKey = "random")

        coVerify {
            settingsRepository.fetchAndPersistAppSettings(expectedUserId)
        }
    }

    @Test
    fun login_callsFetchAndPersistMuteList() = runTest {
        val expectedUserId = "b10a23"
        val authRepository = mockk<AuthRepository>(relaxed = true) {
            coEvery { login(any()) } returns expectedUserId
        }
        val mutedUserRepository = mockk<MutedUserRepository>(relaxed = true)
        val loginHandler = createLoginHandler(
            authRepository = authRepository,
            mutedUserRepository = mutedUserRepository,
        )
        loginHandler.loginAndReturnDefaultFeed(nostrKey = "random")

        coVerify {
            mutedUserRepository.fetchAndPersistMuteList(expectedUserId)
        }
    }

    @Test
    fun login_revertsLoginData_ifAnyOfApiCallsFail() = runTest {
        val nsec = "nsec1p64ty2pgcj6k2c6v7u9dwu7aesle8v9qelnpgx4zrfa37av8f24qyftvle"
        val credentialsPersistence = FakeDataStore(emptyList<Credential>())
        val credentialsStore = CredentialsStore(persistence = credentialsPersistence)

        val activeAccountPersistence = FakeDataStore(initialValue = "")
        val activeAccountStore = ActiveAccountStore(
            accountsStore = mockk(relaxed = true),
            persistence = activeAccountPersistence,
        )

        val authRepository = AuthRepository(
            credentialsStore = credentialsStore,
            activeAccountStore = activeAccountStore,
            userRepository = mockk(relaxed = true),
        )

        val userRepository = mockk<UserRepository>(relaxed = true) {
            coEvery { fetchAndUpdateUserAccount(any()) } throws WssException()
        }
        val loginHandler = createLoginHandler(
            authRepository = authRepository,
            userRepository = userRepository,
        )

        try {
            loginHandler.loginAndReturnDefaultFeed(nostrKey = nsec)
        } catch (_: WssException) { }

        credentialsPersistence.latestData shouldBe emptyList()
        activeAccountPersistence.latestData shouldBe ""
    }
}
