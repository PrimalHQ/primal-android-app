package net.primal.android.notifications.list

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.notary.NostrSignUnauthorized
import net.primal.android.notifications.repository.NotificationRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NotificationsViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private fun createViewModel(
        notificationsRepository: NotificationRepository = mockk(relaxed = true),
    ): NotificationsViewModel {
        return NotificationsViewModel(
            dispatcherProvider = coroutineTestRule.dispatcherProvider,
            activeAccountStore = mockk(relaxed = true),
            notificationsRepository = notificationsRepository,
            noteRepository = mockk(relaxed = true),
            profileRepository = mockk(relaxed = true),
            zapHandler = mockk(relaxed = true),
            subscriptionsManager = mockk(relaxed = true),
        )
    }

    @Test
    fun handleNotificationsSeen_handlesWssException() =
        runTest {
            val viewModel = createViewModel(
                notificationsRepository = mockk(relaxed = true) {
                    coEvery { markAllNotificationsAsSeen() } throws WssException()
                },
            )
            viewModel.setEvent(NotificationsContract.UiEvent.NotificationsSeen)
        }

    @Test
    fun handleNotificationsSeen_doesNotCrashIfUserSignsOutOnNotificationsScreen() =
        runTest {
            val viewModel = createViewModel(
                notificationsRepository = mockk(relaxed = true) {
                    coEvery { markAllNotificationsAsSeen() } throws NostrSignUnauthorized()
                },
            )
            viewModel.setEvent(NotificationsContract.UiEvent.NotificationsSeen)
        }
}
