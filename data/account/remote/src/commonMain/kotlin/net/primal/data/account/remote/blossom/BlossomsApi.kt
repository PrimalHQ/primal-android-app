package net.primal.data.account.remote.blossom

interface BlossomsApi {
    suspend fun getRecommendedBlossomServers(): List<String>
}
