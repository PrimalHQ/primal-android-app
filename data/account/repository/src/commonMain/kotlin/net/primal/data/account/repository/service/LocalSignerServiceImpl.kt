package net.primal.data.account.repository.service

import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.data.account.repository.builder.LocalSignerMethodResponseBuilder
import net.primal.data.account.repository.repository.InternalPermissionsRepository
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.service.LocalSignerService

class LocalSignerServiceImpl(
    private val localAppRepository: LocalAppRepository,
    private val localSignerMethodResponseBuilder: LocalSignerMethodResponseBuilder,
    private val internalPermissionsRepository: InternalPermissionsRepository,
) : LocalSignerService {
    override suspend fun processMethod(method: LocalSignerMethod): Result<LocalSignerMethodResponse> =
        localSignerMethodResponseBuilder.build(method = method)

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

            localAppRepository
                .upsertApp(
                    app = app.copy(
                        permissions = (addedPermissions + app.permissions)
                            .distinctBy { it.permissionId },
                    ),
                )
                .getOrThrow()
        }
}
