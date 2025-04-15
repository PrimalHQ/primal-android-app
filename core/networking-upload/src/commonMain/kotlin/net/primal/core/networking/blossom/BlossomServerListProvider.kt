package net.primal.core.networking.blossom

interface BlossomServerListProvider {
    suspend fun provideBlossomServerList(userId: String): List<String>
}
