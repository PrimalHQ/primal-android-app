package net.primal.android.navigation.splash

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.core.config.AppConfigHandler
import net.primal.core.testing.CoroutinesTestRule
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.feeds.FeedsRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SplashViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val nsecUserId = "aa4fc8665f5696e33db7e1a572e3b0f5b3d615837b0f362dcb1c8068b098c7b4"

    private fun createViewModel(
        activeAccountStore: ActiveAccountStore = mockk(relaxed = true),
        credentialsStore: CredentialsStore = mockk(relaxed = true),
        feedsRepository: FeedsRepository = mockk(relaxed = true),
    ): SplashViewModel {
        return SplashViewModel(
            activeAccountStore = activeAccountStore,
            appConfigHandler = mockk<AppConfigHandler>(relaxed = true),
            credentialsStore = credentialsStore,
            feedsRepository = feedsRepository,
        )
    }

    private fun loggedInAccountStore(userId: String = nsecUserId): ActiveAccountStore =
        mockk(relaxed = true) {
            every { activeUserId() } returns userId
        }

    @Test
    fun start_withLoggedOutUser_skipsFeedsFetchAndCompletesAuthCheck() =
        runTest {
            val feedsRepository = mockk<FeedsRepository>(relaxed = true)
            val viewModel = createViewModel(
                activeAccountStore = mockk(relaxed = true) {
                    every { activeUserId() } returns ""
                },
                feedsRepository = feedsRepository,
            )

            viewModel.start(prefetchFeeds = true)
            advanceUntilIdle()

            coVerify(exactly = 0) { feedsRepository.fetchAndPersistNoteFeeds(any()) }
            viewModel.isLoggedIn.value shouldBe false
            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_withPrefetchDisabled_skipsFeedsFetchForLoggedInUser() =
        runTest {
            val feedsRepository = mockk<FeedsRepository>(relaxed = true)
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                feedsRepository = feedsRepository,
            )

            viewModel.start(prefetchFeeds = false)
            advanceUntilIdle()

            coVerify(exactly = 0) { feedsRepository.fetchAndPersistNoteFeeds(any()) }
            viewModel.isLoggedIn.value shouldBe true
            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_withExternalSignerAccount_skipsFeedsFetch() =
        runTest {
            val feedsRepository = mockk<FeedsRepository>(relaxed = true)
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                credentialsStore = mockk(relaxed = true) {
                    every { isExternalSignerCredential(any()) } returns true
                },
                feedsRepository = feedsRepository,
            )

            viewModel.start(prefetchFeeds = true)
            advanceUntilIdle()

            coVerify(exactly = 0) { feedsRepository.fetchAndPersistNoteFeeds(any()) }
            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_withNsecAccount_fetchesNoteFeedsAndCompletesAuthCheck() =
        runTest {
            val feedsRepository = mockk<FeedsRepository>(relaxed = true)
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                credentialsStore = mockk(relaxed = true) {
                    every { isExternalSignerCredential(any()) } returns false
                },
                feedsRepository = feedsRepository,
            )

            viewModel.start(prefetchFeeds = true)
            advanceUntilIdle()

            coVerify(exactly = 1) { feedsRepository.fetchAndPersistNoteFeeds(userId = nsecUserId) }
            viewModel.isLoggedIn.value shouldBe true
            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_whenFeedsFetchThrows_stillCompletesAuthCheck() =
        runTest {
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                credentialsStore = mockk(relaxed = true) {
                    every { isExternalSignerCredential(any()) } returns false
                },
                feedsRepository = mockk(relaxed = true) {
                    coEvery { fetchAndPersistNoteFeeds(any()) } throws NetworkException()
                },
            )

            viewModel.start(prefetchFeeds = true)
            advanceUntilIdle()

            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_whenFeedsFetchHangs_timesOutAndCompletesAuthCheck() =
        runTest {
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                credentialsStore = mockk(relaxed = true) {
                    every { isExternalSignerCredential(any()) } returns false
                },
                feedsRepository = mockk(relaxed = true) {
                    coEvery { fetchAndPersistNoteFeeds(any()) } coAnswers { delay(10.minutes) }
                },
            )

            viewModel.start(prefetchFeeds = true)
            advanceUntilIdle()

            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_withNsecAccount_blocksAuthCheckUntilFeedsFetchCompletes() =
        runTest {
            val deferred = CompletableDeferred<Unit>()
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                credentialsStore = mockk(relaxed = true) {
                    every { isExternalSignerCredential(any()) } returns false
                },
                feedsRepository = mockk(relaxed = true) {
                    coEvery { fetchAndPersistNoteFeeds(any()) } coAnswers { deferred.await() }
                },
            )

            viewModel.start(prefetchFeeds = true)
            runCurrent()

            viewModel.isAuthCheckComplete.value shouldBe false

            deferred.complete(Unit)
            advanceUntilIdle()

            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_whenCredentialLookupThrows_skipsFeedsFetchAndCompletesAuthCheck() =
        runTest {
            val feedsRepository = mockk<FeedsRepository>(relaxed = true)
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                credentialsStore = mockk(relaxed = true) {
                    every { isExternalSignerCredential(any()) } throws IllegalStateException("datastore")
                },
                feedsRepository = feedsRepository,
            )

            viewModel.start(prefetchFeeds = true)
            advanceUntilIdle()

            coVerify(exactly = 0) { feedsRepository.fetchAndPersistNoteFeeds(any()) }
            viewModel.isAuthCheckComplete.value shouldBe true
        }

    @Test
    fun start_calledTwice_runsWorkOnlyOnce() =
        runTest {
            val feedsRepository = mockk<FeedsRepository>(relaxed = true)
            val viewModel = createViewModel(
                activeAccountStore = loggedInAccountStore(),
                credentialsStore = mockk(relaxed = true) {
                    every { isExternalSignerCredential(any()) } returns false
                },
                feedsRepository = feedsRepository,
            )

            viewModel.start(prefetchFeeds = true)
            viewModel.start(prefetchFeeds = true)
            advanceUntilIdle()

            coVerify(exactly = 1) { feedsRepository.fetchAndPersistNoteFeeds(any()) }
        }
}
