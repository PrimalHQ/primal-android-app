package net.primal.data.account.repository.service

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import net.primal.core.utils.Result
import net.primal.core.utils.add
import net.primal.core.utils.asSuccess
import net.primal.core.utils.remove
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.repository.builder.LocalSignerMethodResponseBuilder
import net.primal.data.account.repository.mappers.asDomain
import net.primal.data.account.repository.mappers.buildSessionEventData
import net.primal.data.account.repository.mappers.getRequestType
import net.primal.data.account.repository.repository.internal.InternalPermissionsRepository
import net.primal.data.account.repository.repository.internal.InternalSessionEventRepository
import net.primal.data.account.repository.repository.internal.InternalSessionRepository
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.domain.account.service.LocalSignerError
import net.primal.domain.account.service.LocalSignerService

@OptIn(ExperimentalAtomicApi::class)
class LocalSignerServiceImpl internal constructor(
    private val localAppRepository: LocalAppRepository,
    private val permissionsRepository: PermissionsRepository,
    private val localSignerMethodResponseBuilder: LocalSignerMethodResponseBuilder,
    private val internalPermissionsRepository: InternalPermissionsRepository,
    private val internalSessionRepository: InternalSessionRepository,
    private val internalSessionEventRepository: InternalSessionEventRepository,
) : LocalSignerService {

    private val responses = AtomicReference<List<LocalSignerMethodResponse>>(emptyList())
    private val pendingUserActionMethods = MutableStateFlow<List<LocalSignerMethod>>(emptyList())

    override suspend fun processMethod(method: LocalSignerMethod): Result<LocalSignerMethodResponse> {
        val permissionAction = localAppRepository.getPermissionActionForMethod(method = method)

        return when (permissionAction) {
            AppPermissionAction.Approve -> {
                val response = localSignerMethodResponseBuilder.build(method = method).also { response ->
                    responses.add(response)
                }
                val session = internalSessionRepository.getOrCreateLocalAppSession(
                    appIdentifier = method.getIdentifier(),
                )
                internalSessionEventRepository.saveLocalSessionEvent(
                    sessionId = session.sessionId,
                    requestType = method.getRequestType(),
                    method = method,
                    response = response,
                )
                response.asSuccess()
            }

            AppPermissionAction.Deny -> {
                Result.failure(LocalSignerError.AutoDenied)
            }

            AppPermissionAction.Ask -> {
                pendingUserActionMethods.add(method)
                Result.failure(LocalSignerError.UserApprovalRequired)
            }
        }
    }

    override fun observeSessionEventsPendingUserAction(): Flow<List<SessionEvent>> =
        pendingUserActionMethods.asStateFlow()
            .map {
                it.mapNotNull { method ->
                    buildSessionEventData(
                        sessionId = "null",
                        processedAt = Clock.System.now().epochSeconds,
                        requestType = method.getRequestType(),
                        method = method,
                        response = null,
                        requestState = AppRequestState.PendingUserAction,
                    )?.asDomain()
                }
            }

    override fun getMethodResponses() = responses.load()

    override suspend fun respondToUserActions(eventChoices: List<SessionEventUserChoice>) {
        eventChoices.forEach { respondToUserAction(eventChoice = it) }
    }

    private suspend fun respondToUserAction(eventChoice: SessionEventUserChoice) {
        val method = pendingUserActionMethods.value
            .firstOrNull { it.eventId == eventChoice.sessionEventId }
            ?: return

        when (eventChoice.userChoice) {
            UserChoice.Allow -> allowMethod(method)

            UserChoice.Reject -> rejectMethod(eventId = method.eventId)

            UserChoice.AlwaysAllow -> {
                allowMethod(method)
                updatePermissionPreference(
                    permissionId = method.getPermissionId(),
                    packageName = method.packageName,
                    action = AppPermissionAction.Approve,
                )
            }

            UserChoice.AlwaysReject -> {
                rejectMethod(eventId = method.eventId)
                updatePermissionPreference(
                    permissionId = method.getPermissionId(),
                    packageName = method.packageName,
                    action = AppPermissionAction.Deny,
                )
            }
        }

        pendingUserActionMethods.remove(method)
    }

    private suspend fun allowMethod(method: LocalSignerMethod) {
        responses.add(localSignerMethodResponseBuilder.build(method))
    }

    private fun rejectMethod(eventId: String) {
        responses.add(
            LocalSignerMethodResponse.Error(
                eventId = eventId,
                message = "User rejected this event.",
            ),
        )
    }

    private suspend fun updatePermissionPreference(
        permissionId: String,
        packageName: String,
        action: AppPermissionAction,
    ) = permissionsRepository.updatePermissionsAction(
        permissionIds = listOf(permissionId),
        clientPubKey = packageName,
        action = action,
    )

    override suspend fun addNewApp(app: LocalApp): Result<Unit> =
        runCatching {
            val addedPermissions = if (app.trustLevel == TrustLevel.Medium) {
                internalPermissionsRepository.getMediumTrustPermissions().getOrThrow()
            } else {
                emptyList()
            }.map { permissionId ->
                AppPermission(
                    permissionId = permissionId,
                    clientPubKey = app.identifier,
                    action = AppPermissionAction.Approve,
                )
            }

            localAppRepository
                .upsertApp(
                    app = app.copy(
                        permissions = (
                            addedPermissions + app.permissions.map { it.copy(clientPubKey = app.identifier) }
                            ).distinctBy { it.permissionId },
                    ),
                )
                .getOrThrow()
        }
}
