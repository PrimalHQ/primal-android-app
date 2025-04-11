package net.primal.domain.user

interface UserDataCleanupRepository {
    suspend fun clearUserData(userId: String)
}
