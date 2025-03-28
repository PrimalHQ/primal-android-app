package net.primal.domain.repository

import kotlinx.coroutines.flow.Flow
import net.primal.domain.UserProfileSearchItem
import net.primal.domain.model.ProfileData
import net.primal.domain.model.ProfileStats

interface ProfileRepository {
    suspend fun fetchProfileId(primalName: String): String?
    suspend fun findProfileDataOrNull(profileId: String): ProfileData?
    suspend fun findProfileData(profileIds: List<String>): List<List<ProfileData>>
    fun observeProfileData(profileId: String): Flow<ProfileData?>
    fun observeProfileData(profileIds: List<String>): Flow<List<ProfileData>>
    fun observeProfileStats(profileId: String): Flow<ProfileStats?>
    suspend fun fetchProfile(profileId: String)
    suspend fun fetchUserProfileFollowedBy(
        profileId: String,
        userId: String,
        limit: Int,
    ): List<ProfileData>
    suspend fun isUserFollowing(userId: String, targetUserId: String): Boolean
    suspend fun fetchFollowers(profileId: String): List<UserProfileSearchItem>
    suspend fun fetchFollowing(profileId: String): List<UserProfileSearchItem>
}
