package net.primal.domain.premium

import net.primal.core.utils.Result

interface PremiumBroadcastRepository {
    suspend fun fetchContentStats(userId: String): Result<Map<Int, Long>>

    suspend fun startBroadcast(userId: String, kinds: List<Int>?): Result<Unit>

    suspend fun cancelBroadcast(userId: String): Result<Unit>

    suspend fun fetchBroadcastStatus(userId: String): Result<BroadcastingStatus>
}
