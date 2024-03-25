package net.primal.android.auth.repository

import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun login_callsSetActiveUserId_beforeCreateNewUserAccount() = runTest {
        val activeAccountStore = mockk<ActiveAccountStore>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val repository = AuthRepository(
            credentialsStore = mockk(relaxed = true),
            activeAccountStore = activeAccountStore,
            userRepository = userRepository,
        )

        repository.login("nsec123456789")

        // This specific order is important because ActiveAccountStore
        // otherwise will not propagate changes immediately
        coVerifyOrder {
            activeAccountStore.setActiveUserId(any())
            userRepository.createNewUserAccount(any())
        }
    }
}
