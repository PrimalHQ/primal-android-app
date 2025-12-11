package net.primal.data.account.repository.service

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.data.account.repository.repository.InternalPermissionsRepository
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.LocalConnectionRepository
import net.primal.domain.account.service.LocalSignerService

class LocalSignerServiceImpl(
    private val localConnectionRepository: LocalConnectionRepository,
    private val internalPermissionsRepository: InternalPermissionsRepository,
) : LocalSignerService {
    override suspend fun respondToMethods(methods: List<LocalSignerMethod>): List<Result<LocalSignerMethodResponse>> {
        TODO("Not yet implemented")
    }

    override fun observePendingActions(): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun getResults(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun addNewApp(app: LocalApp): Result<Unit> =
        runCatching {
            val addedPermissions = if (app.trustLevel == TrustLevel.Medium) {
                internalPermissionsRepository.getMediumTrustPermissions().getOrThrow()
            } else {
                emptyList()
            }.map { permissionId ->
                AppPermission(
                    permissionId = permissionId,
                    clientPubKey = app.packageName,
                    action = PermissionAction.Approve,
                )
            }

            localConnectionRepository
                .upsertApp(app = app.copy(permissions = app.permissions + addedPermissions))
                .getOrThrow()
        }
}
