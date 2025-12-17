package net.primal.data.account.repository.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.PermissionAction
import net.primal.data.account.local.dao.TrustLevel
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asPO
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
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
                    database.permissions().upsertAll(data = app.permissions.map { it.asPO() })
                }
            }
        }

    override suspend fun canProcessMethod(method: LocalSignerMethod): Boolean =
        withContext(dispatchers.io()) {
            val app = method.extractUserPubKey()?.let { userPubKey ->
                database.localApps().findApp(identifier = "${method.packageName}:$userPubKey")
            } ?: return@withContext false

            when (app.data.trustLevel) {
                TrustLevel.Full -> true
                TrustLevel.Medium -> {
                    val permission = app.permissions.firstOrNull { it.permissionId == method.getPermissionId() }

                    when (permission?.action) {
                        PermissionAction.Approve -> true
                        PermissionAction.Deny, PermissionAction.Ask, null -> false
                    }
                }

                TrustLevel.Low -> false
            }
        }
}
