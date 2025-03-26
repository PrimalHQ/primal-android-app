package net.primal.domain.repository

import kotlinx.coroutines.flow.Flow
import net.primal.domain.model.ProfileData

interface MutedUserRepository {
    fun observeMutedUsersByOwnerId(ownerId: String): Flow<List<ProfileData>>
    fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String): Flow<Boolean>
    suspend fun fetchAndPersistMuteList(userId: String)
    suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String)
    suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String)
}
