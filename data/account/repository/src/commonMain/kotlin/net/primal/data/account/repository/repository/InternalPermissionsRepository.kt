package net.primal.data.account.repository.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.account.remote.api.WellKnownApi

class InternalPermissionsRepository(
    private val wellKnownApi: WellKnownApi,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun getMediumTrustPermissions(): Result<List<String>> =
        withContext(dispatchers.io()) {
            runCatching {
                wellKnownApi.getMediumTrustPermissions().allowPermissions
            }
        }
}
