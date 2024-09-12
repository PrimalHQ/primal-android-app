package net.primal.android.profile.repository

import androidx.room.withTransaction
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.explore.domain.UserProfileSearchItem
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.asProfileStatsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.takeContentAsPrimalUserFollowersCountsOrNull
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.db.ProfileInteraction
import net.primal.android.profile.report.ReportType
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.PublicBookmark
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.asUserAccountFromBookmarksListEvent
import net.primal.android.user.domain.asUserAccountFromFollowListEvent
import net.primal.android.user.repository.UserRepository

class ProfileRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val userRepository: UserRepository,
    private val userAccountFetcher: UserAccountFetcher,
    private val nostrPublisher: NostrPublisher,
) {

    suspend fun findProfileDataOrNull(profileId: String) =
        withContext(dispatchers.io()) {
            database.profiles().findProfileData(profileId = profileId)
        }

    suspend fun findProfilesData(profileIds: List<String>) =
        withContext(dispatchers.io()) {
            database.profiles().findProfileData(profileIds = profileIds)
        }

    fun observeProfile(profileId: String) = database.profiles().observeProfile(profileId = profileId).filterNotNull()

    fun observeProfileData(profileId: String) =
        database.profiles().observeProfileData(profileId = profileId).filterNotNull()

    suspend fun fetchUserProfileFollowedBy(
        userProfileId: String,
        userId: String,
        limit: Int,
    ): List<ProfileData> {
        val users = usersApi.getUserProfileFollowedBy(userProfileId, userId, limit)

        val profiles = users.mapNotNull { user ->
            val cdnResources = user.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            user.metadata?.asProfileDataPO(cdnResources = cdnResources)
        }

        withContext(dispatchers.io()) {
            database.profiles().upsertAll(data = profiles)
        }

        return profiles
    }

    suspend fun observeProfilesData(profileIds: List<String>) =
        withContext(dispatchers.io()) {
            database.profiles().observeProfilesData(profileIds = profileIds).filterNotNull()
        }

    fun observeProfileStats(profileId: String) =
        database.profileStats().observeProfileStats(profileId = profileId).filterNotNull()

    suspend fun requestProfileUpdate(profileId: String) {
        val response = usersApi.getUserProfile(userId = profileId)
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profileMetadata = response.metadata?.asProfileDataPO(cdnResources = cdnResources)
        val profileStats = response.profileStats?.asProfileStatsPO()

        database.withTransaction {
            if (profileMetadata != null) {
                database.profiles().upsertAll(data = listOf(profileMetadata))
            }

            if (profileStats != null) {
                database.profileStats().upsert(data = profileStats)
            }
        }
    }

    @Throws(FollowListNotFound::class, NostrPublishException::class)
    suspend fun follow(userId: String, followedUserId: String) {
        updateFollowList(userId = userId) {
            toMutableSet().apply { add(followedUserId) }
        }
    }

    @Throws(FollowListNotFound::class, NostrPublishException::class)
    suspend fun unfollow(userId: String, unfollowedUserId: String) {
        updateFollowList(userId = userId) {
            toMutableSet().apply { remove(unfollowedUserId) }
        }
    }

    @Throws(FollowListNotFound::class, NostrPublishException::class)
    private suspend fun updateFollowList(userId: String, reducer: Set<String>.() -> Set<String>) {
        val userFollowList = userAccountFetcher.fetchUserFollowListOrNull(userId = userId)
            ?: throw FollowListNotFound()

        userRepository.updateFollowList(userId, userFollowList)

        setFollowList(
            userId = userId,
            contacts = userFollowList.following.reducer(),
            content = userFollowList.followListEventContent ?: "",
        )
    }

    @Throws(NostrPublishException::class)
    suspend fun setFollowList(
        userId: String,
        contacts: Set<String>,
        content: String = "",
    ) {
        val nostrEventResponse = nostrPublisher.publishUserFollowList(
            userId = userId,
            contacts = contacts,
            content = content,
        )
        userRepository.updateFollowList(
            userId = userId,
            contactsUserAccount = nostrEventResponse.asUserAccountFromFollowListEvent(),
        )
    }

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    suspend fun addBookmark(
        userId: String,
        forceUpdate: Boolean,
        bookmark: PublicBookmark,
    ) {
        updateBookmarksList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { add(bookmark) }
        }
    }

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    suspend fun removeBookmark(
        userId: String,
        forceUpdate: Boolean,
        bookmark: PublicBookmark,
    ) {
        updateBookmarksList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { remove(bookmark) }
        }
    }

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    private suspend fun updateBookmarksList(
        userId: String,
        forceUpdate: Boolean,
        reducer: Set<PublicBookmark>.() -> Set<PublicBookmark>,
    ) {
        val bookmarksList = userAccountFetcher.fetchUserBookmarksListOrNull(userId = userId)
            ?: if (forceUpdate) UserAccount.EMPTY else throw BookmarksListNotFound()

        userRepository.updateBookmarksList(userId, bookmarksList)

        setBookmarksList(userId = userId, bookmarks = bookmarksList.bookmarks.reducer())
    }

    @Throws(NostrPublishException::class)
    suspend fun setBookmarksList(userId: String, bookmarks: Set<PublicBookmark>) {
        val nostrEventResponse = nostrPublisher.publishUserBookmarksList(
            userId = userId,
            bookmarks = bookmarks,
        )
        userRepository.updateBookmarksList(
            userId = userId,
            bookmarksUserAccount = nostrEventResponse.asUserAccountFromBookmarksListEvent(),
        )
    }

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> {
        val response = apiBlock()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profiles = response.contactsMetadata.mapAsProfileDataPO(cdnResources = cdnResources)
        val followersCountsMap = response.followerCounts?.takeContentAsPrimalUserFollowersCountsOrNull()

        database.profiles().upsertAll(data = profiles)

        return profiles.map {
            val score = followersCountsMap?.get(it.ownerId)
            UserProfileSearchItem(metadata = it, followersCount = score)
        }.sortedByDescending { it.followersCount }
    }

    suspend fun fetchFollowers(userId: String) =
        queryRemoteUsers {
            usersApi.getUserFollowers(userId = userId)
        }

    suspend fun fetchFollowing(userId: String) =
        queryRemoteUsers {
            usersApi.getUserFollowing(userId = userId)
        }

    @Throws(NostrPublishException::class)
    suspend fun reportAbuse(
        userId: String,
        reportType: ReportType,
        profileId: String,
        noteId: String? = null,
    ) {
        nostrPublisher.publishReportAbuseEvent(
            userId = userId,
            reportType = reportType,
            reportProfileId = profileId,
            reportNoteId = noteId,
        )
    }

    suspend fun isUserFollowing(userId: String, targetUserId: String) = usersApi.isUserFollowing(userId, targetUserId)

    fun markAsInteracted(profileId: String) {
        database.profileInteractions().insertOrUpdate(
            ProfileInteraction(
                profileId = profileId,
                lastInteractionAt = Instant.now().epochSecond,
            ),
        )
    }

    class FollowListNotFound : Exception()

    class BookmarksListNotFound : Exception()
}
