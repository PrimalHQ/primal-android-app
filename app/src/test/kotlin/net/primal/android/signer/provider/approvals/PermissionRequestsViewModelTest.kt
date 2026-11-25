package net.primal.android.signer.provider.approvals

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.signer.provider.LOCAL_SIGNER_METHOD
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.SideEffect
import net.primal.android.signer.provider.asNavArgument
import net.primal.core.testing.CoroutinesTestRule
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.data.account.repository.service.LocalSignerError
import net.primal.data.account.repository.service.LocalSignerService
import net.primal.data.account.signer.local.model.LocalSignerMethod
import net.primal.data.account.signer.local.model.LocalSignerMethodResponse
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.repository.SessionEventRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PermissionRequestsViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val pubkey = "a".repeat(64)

    private val method = LocalSignerMethod.Nip44Decrypt(
        eventId = "event-id",
        packageName = "com.example.app",
        requestedAt = 0L,
        userPubKey = pubkey,
        otherPubKey = pubkey,
        ciphertext = "ciphertext",
    )

    private fun createViewModel(localSignerService: LocalSignerService): PermissionRequestsViewModel =
        PermissionRequestsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(LOCAL_SIGNER_METHOD to method.asNavArgument())),
            localSignerService = localSignerService,
            dispatcherProvider = coroutineTestRule.dispatcherProvider,
            permissionsRepository = mockk<PermissionsRepository> {
                coEvery { getNamingMap() } returns emptyMap<String, String>().asSuccess()
            },
            sessionEventRepository = mockk<SessionEventRepository> {
                every { observeEventsPendingUserActionForLocalApp(any()) } returns emptyFlow()
            },
            localAppRepository = mockk<LocalAppRepository> {
                every { observeApp(any()) } returns flowOf(null)
            },
        )

    private fun localSignerService(
        processResult: Result<Unit>,
        hasPendingMethods: Boolean,
        responses: List<LocalSignerMethodResponse>,
    ): LocalSignerService =
        mockk {
            coEvery { processMethodOrAddToPending(any()) } returns processResult
            every { hasPendingMethods() } returns hasPendingMethods
            every { getAllMethodResponses() } returns responses
        }

    private fun TestScope.collectEffects(viewModel: PermissionRequestsViewModel): List<SideEffect> {
        val effects = mutableListOf<SideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        return effects
    }

    @Test
    fun requestApprovedByStoredPermission_sendsResponsesWithoutUserAction() =
        runTest {
            val response = LocalSignerMethodResponse.Success.Nip44Decrypt(eventId = method.eventId, plaintext = "gm")
            val viewModel = createViewModel(
                localSignerService = localSignerService(
                    processResult = Unit.asSuccess(),
                    hasPendingMethods = false,
                    responses = listOf(response),
                ),
            )
            val effects = collectEffects(viewModel)

            advanceUntilIdle()

            effects shouldBe listOf(SideEffect.RequestsCompleted(approved = listOf(response), rejected = emptyList()))
        }

    @Test
    fun requestDeniedByStoredPermission_sendsResponsesWithoutUserAction() =
        runTest {
            val response = LocalSignerMethodResponse.Error(eventId = method.eventId, message = "auto denied")
            val viewModel = createViewModel(
                localSignerService = localSignerService(
                    processResult = Result.failure(LocalSignerError.AutoDenied()),
                    hasPendingMethods = false,
                    responses = listOf(response),
                ),
            )
            val effects = collectEffects(viewModel)

            advanceUntilIdle()

            effects shouldBe listOf(SideEffect.RequestsCompleted(approved = emptyList(), rejected = listOf(response)))
        }

    @Test
    fun requestWaitingForUser_doesNotSendResponses() =
        runTest {
            val viewModel = createViewModel(
                localSignerService = localSignerService(
                    processResult = Unit.asSuccess(),
                    hasPendingMethods = true,
                    responses = emptyList(),
                ),
            )
            val effects = collectEffects(viewModel)

            advanceUntilIdle()

            effects.shouldBeEmpty()
        }

    @Test
    fun requestFromUnknownApp_isInvalidAndSendsNoResponses() =
        runTest {
            val viewModel = createViewModel(
                localSignerService = localSignerService(
                    processResult = Result.failure(LocalSignerError.AppNotFound()),
                    hasPendingMethods = false,
                    responses = emptyList(),
                ),
            )
            val effects = collectEffects(viewModel)

            advanceUntilIdle()

            effects shouldBe listOf(SideEffect.InvalidRequest)
        }
}
