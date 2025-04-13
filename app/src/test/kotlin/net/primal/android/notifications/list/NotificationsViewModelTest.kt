package net.primal.android.notifications.list

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.notifications.NotificationRepository
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
            notificationRepository = notificationsRepository,
            subscriptionsManager = mockk(relaxed = true),
            nostrNotary = mockk(relaxed = true),
        )
    }

    @Test
    fun handleNotificationsSeen_handlesWssException() =
        runTest {
            val viewModel = createViewModel(
                notificationsRepository = mockk(relaxed = true) {
                    coEvery { markAllNotificationsAsSeen(any()) } throws WssException()
                },
            )
            viewModel.setEvent(NotificationsContract.UiEvent.NotificationsSeen)
        }

    @Test
    fun handleNotificationsSeen_doesNotCrashIfUserSignsOutOnNotificationsScreen() =
        runTest {
            val viewModel = createViewModel(
                notificationsRepository = mockk(relaxed = true) {
                    coEvery { markAllNotificationsAsSeen(any()) } throws SigningRejectedException()
                },
            )
            viewModel.setEvent(NotificationsContract.UiEvent.NotificationsSeen)
        }
}
