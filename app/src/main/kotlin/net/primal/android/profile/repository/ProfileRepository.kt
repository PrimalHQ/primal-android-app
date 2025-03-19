package net.primal.android.profile.repository

import androidx.room.withTransaction
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
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
import net.primal.android.nostr.ext.mapAsMapPubkeyToListOfBlossomServers
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.ext.takeContentAsPrimalUserFollowersCountsOrNull
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.profile.api.ProfileWellKnownApi
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.db.ProfileInteraction
import net.primal.android.profile.report.ReportType
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.asUserAccountFromFollowListEvent
import net.primal.android.user.repository.UserRepository
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.domain.nostr.NostrEventKind

class ProfileRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val wellKnownApi: ProfileWellKnownApi,
    private val userRepository: UserRepository,
    private val userAccountFetcher: UserAccountFetcher,
    private val nostrPublisher: NostrPublisher,
) {

    suspend fun fetchProfileId(primalName: String): String? =
        withContext(dispatchers.io()) {
            wellKnownApi.fetchProfileId(primalName = primalName).names[primalName]
        }

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
            val primalPremiumInfo = users.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = users.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = users.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val blossomServers = users.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profiles = users.metadataEvents.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
            database.profiles().insertOrUpdateAll(data = profiles)
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
            val response = retryNetworkCall { usersApi.getUserProfile(userId = profileId) }
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val primalUserName = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profileMetadata = response.metadata?.asProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserName,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
            val profileStats = response.profileStats?.asProfileStatsPO()

            database.withTransaction {
                if (profileMetadata != null) {
                    database.profiles().insertOrUpdateAll(data = listOf(profileMetadata))
                }

                if (profileStats != null) {
                    database.profileStats().upsert(data = profileStats)
                }
            }
        }

    @Throws(FollowListNotFound::class, NostrPublishException::class, MissingPrivateKeyException::class)
    suspend fun follow(
        userId: String,
        followedUserId: String,
        forceUpdate: Boolean,
    ) {
        updateFollowList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { add(followedUserId) }
        }
    }

    @Throws(FollowListNotFound::class, NostrPublishException::class, MissingPrivateKeyException::class)
    suspend fun unfollow(
        userId: String,
        unfollowedUserId: String,
        forceUpdate: Boolean,
    ) {
        updateFollowList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { remove(unfollowedUserId) }
        }
    }

    @Throws(FollowListNotFound::class, NostrPublishException::class, MissingPrivateKeyException::class)
    private suspend fun updateFollowList(
        userId: String,
        forceUpdate: Boolean,
        reducer: Set<String>.() -> Set<String>,
    ) = withContext(dispatchers.io()) {
        val userFollowList = userAccountFetcher.fetchUserFollowListOrNull(userId = userId)
        val isEmptyFollowList = userFollowList == null || userFollowList.following.isEmpty()
        if (isEmptyFollowList && !forceUpdate) {
            throw FollowListNotFound()
        }

        if (userFollowList != null) {
            userRepository.updateFollowList(userId, userFollowList)
        }

        val existingFollowing = userFollowList?.following ?: emptySet()
        setFollowList(
            userId = userId,
            contacts = existingFollowing.reducer(),
            content = userFollowList?.followListEventContent ?: "",
        )
    }

    @Throws(NostrPublishException::class, MissingPrivateKeyException::class)
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

    @Throws(NostrPublishException::class, MissingPrivateKeyException::class)
    suspend fun recoverFollowList(
        userId: String,
        tags: List<JsonArray>,
        content: String,
    ) = withContext(dispatchers.io()) {
        val publishResult = nostrPublisher.signPublishImportNostrEvent(
            userId = userId,
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.FollowList.value,
                tags = tags,
                content = content,
            ),
        )

        userRepository.updateFollowList(
            userId = userId,
            contactsUserAccount = publishResult.nostrEvent.asUserAccountFromFollowListEvent(),
        )
    }

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> =
        withContext(dispatchers.io()) {
            val response = apiBlock()
            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profiles = response.contactsMetadata.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
            val followersCountsMap = response.followerCounts?.takeContentAsPrimalUserFollowersCountsOrNull()

            database.profiles().insertOrUpdateAll(data = profiles)

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

    @Throws(NostrPublishException::class, MissingPrivateKeyException::class)
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

            nostrPublisher.signPublishImportNostrEvent(
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

    suspend fun markAsInteracted(profileId: String, ownerId: String) =
        withContext(dispatchers.io()) {
            database.profileInteractions().upsert(
                ProfileInteraction(
                    profileId = profileId,
                    lastInteractionAt = Instant.now().epochSecond,
                    ownerId = ownerId,
                ),
            )
        }

    class FollowListNotFound : Exception()
}
