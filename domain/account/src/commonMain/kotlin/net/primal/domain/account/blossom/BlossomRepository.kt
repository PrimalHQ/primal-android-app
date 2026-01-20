package net.primal.domain.account.blossom

import net.primal.core.utils.Result

interface BlossomRepository {
    suspend fun fetchRecommendedBlossomServers(): Result<List<String>>
}
