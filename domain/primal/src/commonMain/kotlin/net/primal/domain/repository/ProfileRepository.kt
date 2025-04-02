package net.primal.domain.repository

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.UserProfileSearchItem
import net.primal.domain.model.ProfileData
import net.primal.domain.model.ProfileStats
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.NostrPublishException

interface ProfileRepository {
    suspend fun fetchProfileId(primalName: String): String?
    suspend fun findProfileDataOrNull(profileId: String): ProfileData?
    suspend fun findProfileData(profileIds: List<String>): List<ProfileData>
    suspend fun findProfileStats(profileIds: List<String>): List<ProfileStats>

    fun observeProfileData(profileId: String): Flow<ProfileData>
    fun observeProfileData(profileIds: List<String>): Flow<List<ProfileData>>
    fun observeProfileStats(profileId: String): Flow<ProfileStats?>
    suspend fun fetchProfile(profileId: String): ProfileData?

    suspend fun fetchUserProfileFollowedBy(
        profileId: String,
        userId: String,
        limit: Int,
    ): List<ProfileData>

    suspend fun isUserFollowing(userId: String, targetUserId: String): Boolean
    suspend fun fetchFollowers(profileId: String): List<UserProfileSearchItem>
    suspend fun fetchFollowing(profileId: String): List<UserProfileSearchItem>

    @Throws(
        NostrPublishException::class,
        SigningKeyNotFoundException::class,
        SigningRejectedException::class,
        CancellationException::class,
    )
    suspend fun reportAbuse(
        userId: String,
        reportType: ReportType,
        profileId: String,
        eventId: String? = null,
        articleId: String? = null,
    )
}
