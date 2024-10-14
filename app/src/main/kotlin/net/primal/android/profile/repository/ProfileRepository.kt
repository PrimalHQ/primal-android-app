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
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.asProfileStatsPO
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalUserName
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.ext.takeContentAsPrimalUserFollowersCountsOrNull
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.db.ProfileInteraction
import net.primal.android.profile.report.ReportType
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.asUserAccountFromFollowListEvent
import net.primal.android.user.repository.UserRepository
import timber.log.Timber

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
        profileId: String,
        userId: String,
        limit: Int,
    ): List<ProfileData> =
        withContext(dispatchers.io()) {
            val users = usersApi.getUserProfileFollowedBy(profileId, userId, limit)

            val primalUserNames = users.primalUserNames.parseAndMapPrimalUserNames()
            val cdnResources = users.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val profiles =
                users.metadataEvents.mapAsProfileDataPO(cdnResources = cdnResources, primalUserNames = primalUserNames)
            database.profiles().upsertAll(data = profiles)
            profiles
        }

    suspend fun observeProfilesData(profileIds: List<String>) =
        withContext(dispatchers.io()) {
            database.profiles().observeProfilesData(profileIds = profileIds).filterNotNull()
        }

    fun observeProfileStats(profileId: String) =
        database.profileStats().observeProfileStats(profileId = profileId).filterNotNull()

    suspend fun requestProfileUpdate(profileId: String) =
        withContext(dispatchers.io()) {
            val response = usersApi.getUserProfile(userId = profileId)
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val primalUserName = response.primalName.parseAndMapPrimalUserName()
            Timber.tag("profile").i(primalUserName.toString())
            val profileMetadata = response.metadata?.asProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserName,
            )
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
    private suspend fun updateFollowList(userId: String, reducer: Set<String>.() -> Set<String>) =
        withContext(dispatchers.io()) {
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
    ) = withContext(dispatchers.io()) {
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

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> =
        withContext(dispatchers.io()) {
            val response = apiBlock()
            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val profiles = response.contactsMetadata.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
            )
            val followersCountsMap = response.followerCounts?.takeContentAsPrimalUserFollowersCountsOrNull()

            database.profiles().upsertAll(data = profiles)

            profiles.map {
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
        eventId: String? = null,
        articleId: String? = null,
    ) {
        withContext(dispatchers.io()) {
            val profileTag = profileId.asPubkeyTag(optional = if (eventId == null) reportType.id else null)
            val eventTag = eventId?.asEventIdTag(marker = reportType.id)
            val articleTag = articleId?.let {
                "${NostrEventKind.LongFormContent.value}:$profileId:$articleId".asReplaceableEventTag()
            }

            nostrPublisher.signAndPublishNostrEvent(
                userId = userId,
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    content = "",
                    kind = NostrEventKind.Reporting.value,
                    tags = listOfNotNull(profileTag, eventTag, articleTag),
                ),
            )
        }
    }

    suspend fun isUserFollowing(userId: String, targetUserId: String) =
        withContext(dispatchers.io()) {
            usersApi.isUserFollowing(userId, targetUserId)
        }

    fun markAsInteracted(profileId: String) {
        database.profileInteractions().insertOrUpdate(
            ProfileInteraction(
                profileId = profileId,
                lastInteractionAt = Instant.now().epochSecond,
            ),
        )
    }

    class FollowListNotFound : Exception()
}
