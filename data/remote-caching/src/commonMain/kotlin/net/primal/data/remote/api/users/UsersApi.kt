package net.primal.data.remote.api.users

import net.primal.data.remote.api.explore.model.UsersResponse
import net.primal.data.remote.api.users.model.BookmarksResponse
import net.primal.data.remote.api.users.model.UserContactsResponse
import net.primal.data.remote.api.users.model.UserProfileResponse
import net.primal.data.remote.api.users.model.UserProfilesResponse
import net.primal.data.remote.api.users.model.UsersRelaysResponse

interface UsersApi {

    suspend fun getUserProfile(userId: String): UserProfileResponse

    suspend fun getUserProfileFollowedBy(
        profileId: String,
        userId: String,
        limit: Int = 5,
    ): UserProfilesResponse

    suspend fun getUserFollowList(userId: String): UserContactsResponse

    suspend fun getUserProfilesMetadata(userIds: Set<String>): UserProfilesResponse

    suspend fun getUserFollowers(userId: String): UsersResponse

    suspend fun getUserFollowing(userId: String): UsersResponse

    suspend fun getUserRelays(userIds: List<String>): UsersRelaysResponse

    suspend fun getDefaultRelays(): List<String>

    suspend fun isUserFollowing(userId: String, targetUserId: String): Boolean

    suspend fun getUserBookmarksList(userId: String): BookmarksResponse
}
