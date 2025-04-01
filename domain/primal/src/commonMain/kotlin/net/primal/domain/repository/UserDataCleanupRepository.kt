package net.primal.domain.repository

interface UserDataCleanupRepository {
    suspend fun clearUserData(userId: String)
}
