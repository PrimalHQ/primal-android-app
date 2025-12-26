package net.primal.data.account.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.apps.TrustLevel
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.data.account.repository.mappers.asPO
import net.primal.domain.account.model.AppPermissionAction as PermissionActionDO
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.TrustLevel as TrustLevelDO
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.shared.data.local.db.withTransaction

class LocalAppRepositoryImpl(
    private val dispatchers: DispatcherProvider,
    private val database: AccountDatabase,
) : LocalAppRepository {
    override suspend fun upsertApp(app: LocalApp): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                database.withTransaction {
                    database.localApps().upsertAll(data = listOf(app.asPO()))
                    database.appPermissions().upsertAll(data = app.permissions.map { it.asPO() })
                }
            }
        }

    override suspend fun getPermissionActionForMethod(method: LocalSignerMethod): PermissionActionDO =
        withContext(dispatchers.io()) {
            val app = method.extractUserPubKey()?.let { userPubKey ->
                database.localApps().findApp(identifier = "${method.packageName}:$userPubKey")
            } ?: return@withContext PermissionActionDO.Deny

            when (app.data.trustLevel) {
                TrustLevel.Full -> PermissionActionDO.Approve
                TrustLevel.Medium -> {
                    app.permissions.firstOrNull { it.permissionId == method.getPermissionId() }
                        ?.action?.asDomain()
                        ?: PermissionActionDO.Ask
                }

                TrustLevel.Low -> PermissionActionDO.Ask
            }
        }

    override fun observeAllApps(): Flow<List<LocalApp>> =
        database.localApps().observeAll()
            .map { apps -> apps.map { it.asDomain() } }

    override fun observeApp(identifier: String): Flow<LocalApp?> =
        database.localApps().observeApp(identifier)
            .map { it?.asDomain() }

    override suspend fun updateTrustLevel(identifier: String, trustLevel: TrustLevelDO): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                database.localApps().updateTrustLevel(
                    identifier = identifier,
                    trustLevel = trustLevel.asPO(),
                )
            }
        }

    override suspend fun deleteApp(identifier: String): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                database.withTransaction {
                    deleteEverythingForIdentifier(identifier)
                }
            }
        }

    private suspend inline fun deleteEverythingForIdentifier(identifier: String) {
        database.appPermissions().deletePermissions(appIdentifier = identifier)
        database.appSessions().deleteAllSessionsByApp(appIdentifier = identifier)
        database.localApps().deleteApp(identifier = identifier)
        database.localAppSessionEvents().deleteEvents(appIdentifier = identifier)
    }
}
