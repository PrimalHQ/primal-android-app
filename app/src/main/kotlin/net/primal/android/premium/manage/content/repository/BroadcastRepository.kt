package net.primal.android.premium.manage.content.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.premium.manage.content.api.BroadcastApi
import net.primal.core.utils.coroutines.DispatcherProvider

class BroadcastRepository @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val broadcastApi: BroadcastApi,
) {

    suspend fun startBroadcast(userId: String, kinds: List<Int>? = null) =
        withContext(dispatchers.io()) {
            broadcastApi.startContentRebroadcast(userId = userId, kinds = kinds)
        }

    suspend fun cancelBroadcast(userId: String) =
        withContext(dispatchers.io()) {
            broadcastApi.cancelContentRebroadcast(userId = userId)
        }

    suspend fun fetchBroadcastStatus(userId: String) =
        withContext(dispatchers.io()) {
            broadcastApi.getContentRebroadcastStatus(userId)
        }

    suspend fun fetchContentStats(userId: String) =
        withContext(dispatchers.io()) {
            broadcastApi.getContentStats(userId = userId)
        }
}
