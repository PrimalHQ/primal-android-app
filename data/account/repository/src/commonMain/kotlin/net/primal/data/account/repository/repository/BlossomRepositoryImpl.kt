package net.primal.data.account.repository.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.account.remote.blossom.BlossomsApi
import net.primal.domain.account.blossom.BlossomRepository

class BlossomRepositoryImpl(
    private val dispatchers: DispatcherProvider,
    private val blossomsApi: BlossomsApi,
) : BlossomRepository {

    override suspend fun fetchRecommendedBlossomServers(): Result<List<String>> =
        withContext(dispatchers.io()) {
            runCatching { blossomsApi.getRecommendedBlossomServers() }
        }
}
