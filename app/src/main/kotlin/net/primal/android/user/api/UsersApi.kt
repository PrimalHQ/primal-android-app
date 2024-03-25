package net.primal.android.user.api

import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserProfilesResponse
import net.primal.android.user.api.model.UserRelaysResponse

interface UsersApi {

    suspend fun getUserProfile(userId: String): UserProfileResponse

    suspend fun getUserFollowList(userId: String): UserContactsResponse

    suspend fun getUserProfilesMetadata(userIds: Set<String>): UserProfilesResponse

    suspend fun getUserFollowers(userId: String): UsersResponse

    suspend fun getUserFollowing(userId: String): UsersResponse

    suspend fun getUserRelays(userId: String): UserRelaysResponse

    suspend fun getDefaultRelays(): List<String>
}
