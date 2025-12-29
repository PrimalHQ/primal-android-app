package net.primal.data.account.repository.service

import io.github.aakira.napier.Napier
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import net.primal.core.utils.Result
import net.primal.core.utils.add
import net.primal.core.utils.asSuccess
import net.primal.core.utils.map
import net.primal.core.utils.recover
import net.primal.core.utils.remove
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.repository.builders.LocalSignerMethodResponseBuilder
import net.primal.data.account.repository.mappers.getRequestType
import net.primal.data.account.repository.repository.InternalLocalSessionEventRepository
import net.primal.data.account.repository.repository.internal.InternalPermissionsRepository
import net.primal.data.account.repository.repository.internal.InternalSessionRepository
import net.primal.data.account.repository.repository.internal.model.UpdateAppSessionEventRequest
import net.primal.data.account.signer.local.LocalSignerMethod
import net.primal.data.account.signer.local.LocalSignerMethodResponse
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.PermissionsRepository

@OptIn(ExperimentalAtomicApi::class)
internal class LocalSignerServiceImpl internal constructor(
    private val localAppRepository: LocalAppRepository,
    private val permissionsRepository: PermissionsRepository,
    private val localSignerMethodResponseBuilder: LocalSignerMethodResponseBuilder,
    private val internalPermissionsRepository: InternalPermissionsRepository,
    private val internalSessionRepository: InternalSessionRepository,
    private val internalSessionEventRepository: InternalLocalSessionEventRepository,
) : LocalSignerService {

    private val pendingMethods = AtomicReference<List<LocalSignerMethod>>(emptyList())
    private val allResponses = AtomicReference<List<LocalSignerMethodResponse>>(emptyList())

    override suspend fun processMethod(method: LocalSignerMethod): Result<LocalSignerMethodResponse> {
        val permissionAction = localAppRepository.getPermissionActionForMethod(
            appIdentifier = method.getIdentifier(),
            permissionId = method.getPermissionId(),
        )
        return when (permissionAction) {
            AppPermissionAction.Approve -> {
                val response = localSignerMethodResponseBuilder.build(method = method)
                allResponses.add(response)
                insertNewSessionEvent(
                    method = method,
                    requestState = AppRequestState.Approved,
                    response = response,
                )
                response.asSuccess()
            }

            AppPermissionAction.Deny -> {
                val response = LocalSignerMethodResponse.Error(
                    eventId = method.eventId,
                    message = "Request rejected by policy (auto denied).",
                )
                allResponses.add(response)
                insertNewSessionEvent(
                    method = method,
                    requestState = AppRequestState.Rejected,
                    response = response,
                )
                Result.failure(LocalSignerError.AutoDenied)
            }

            AppPermissionAction.Ask -> {
                Result.failure(LocalSignerError.UserApprovalRequired)
            }
        }
    }

    override suspend fun processMethodOrAddToPending(method: LocalSignerMethod): Result<Unit> {
        return processMethod(method).map { Unit }.recover { error ->
            if (error is LocalSignerError.UserApprovalRequired) {
                pendingMethods.add(method)
                insertNewSessionEvent(
                    method = method,
                    requestState = AppRequestState.PendingUserAction,
                    response = null,
                )
                Unit.asSuccess()
            } else {
                Result.failure(error)
            }
        }
    }

    private suspend fun insertNewSessionEvent(
        method: LocalSignerMethod,
        requestState: AppRequestState,
        response: LocalSignerMethodResponse?,
    ) {
        val session = internalSessionRepository.getOrCreateLocalAppSession(
            appIdentifier = method.getIdentifier(),
        )
        internalSessionEventRepository.saveLocalSessionEvent(
            sessionId = session.sessionId,
            requestType = method.getRequestType(),
            method = method,
            response = null,
            requestState = requestState,
        )
    }

    override fun getAllMethodResponses() = allResponses.load()

    override suspend fun respondToUserActions(eventChoices: List<SessionEventUserChoice>) {
        eventChoices.forEach { respondToUserAction(eventChoice = it) }
    }

    private suspend fun respondToUserAction(eventChoice: SessionEventUserChoice) {
        val method = pendingMethods.load()
            .firstOrNull { it.eventId == eventChoice.sessionEventId }
            ?: run {
                Napier.e(tag = "LocalSigner") {
                    "You are trying to respond to a session event " +
                        "from different instance of LocalSignerService."
                }
                return
            }

        when (eventChoice.userChoice) {
            UserChoice.Allow -> approveMethodByUserAction(method)

            UserChoice.Reject -> rejectMethodByUser(eventId = method.eventId)

            UserChoice.AlwaysAllow -> {
                approveMethodByUserAction(method)
                updatePermissionPreference(
                    permissionId = method.getPermissionId(),
                    appIdentifier = method.getIdentifier(),
                    action = AppPermissionAction.Approve,
                )
            }

            UserChoice.AlwaysReject -> {
                rejectMethodByUser(eventId = method.eventId)
                updatePermissionPreference(
                    permissionId = method.getPermissionId(),
                    appIdentifier = method.getIdentifier(),
                    action = AppPermissionAction.Deny,
                )
            }
        }
        pendingMethods.remove(method)
    }

    private suspend fun approveMethodByUserAction(method: LocalSignerMethod) {
        val response = localSignerMethodResponseBuilder.build(method)
        storeResponseAndUpdateSessionEventState(
            response = response,
            requestState = AppRequestState.Approved,
        )
    }

    private suspend fun rejectMethodByUser(eventId: String) {
        val response = LocalSignerMethodResponse.Error(
            eventId = eventId,
            message = "User rejected this event.",
        )
        storeResponseAndUpdateSessionEventState(
            response = response,
            requestState = AppRequestState.Rejected,
        )
    }

    private suspend fun storeResponseAndUpdateSessionEventState(
        response: LocalSignerMethodResponse,
        requestState: AppRequestState,
    ) {
        allResponses.add(response)
        internalSessionEventRepository.updateLocalAppSessionEventState(
            listOf(
                UpdateAppSessionEventRequest(
                    eventId = response.eventId,
                    requestState = requestState,
                    responsePayload = response.encodeToJsonString(),
                    completedAt = Clock.System.now().epochSeconds,
                ),
            ),
        )
    }

    private suspend fun updatePermissionPreference(
        permissionId: String,
        appIdentifier: String,
        action: AppPermissionAction,
    ) {
        val localApp = localAppRepository.getApp(identifier = appIdentifier) ?: return

        if (localApp.trustLevel == TrustLevel.Low && action == AppPermissionAction.Approve) {
            localAppRepository.updateTrustLevel(
                identifier = appIdentifier,
                trustLevel = TrustLevel.Medium,
            )
            permissionsRepository.updatePermissionsActionByAppIdentifier(
                appIdentifier = appIdentifier,
                action = AppPermissionAction.Ask,
            )
        }

        permissionsRepository.upsertPermissionsAction(
            permissionId = permissionId,
            appIdentifier = appIdentifier,
            action = action,
        )
    }

    override suspend fun addNewApp(app: LocalApp): Result<Unit> =
        runCatching {
            val defaultPermissions = if (app.trustLevel == TrustLevel.Medium) {
                internalPermissionsRepository.getMediumTrustPermissions().getOrThrow()
            } else {
                emptyList()
            }.map { permissionId ->
                AppPermission(
                    permissionId = permissionId,
                    appIdentifier = app.identifier,
                    action = AppPermissionAction.Approve,
                )
            }

            val configPermissions = app.permissions.map { it.copy(appIdentifier = app.identifier) }
            val allPermissions = (defaultPermissions + configPermissions).distinctBy { it.permissionId }

            localAppRepository
                .upsertApp(app = app.copy(permissions = allPermissions))
                .getOrThrow()
        }
}
