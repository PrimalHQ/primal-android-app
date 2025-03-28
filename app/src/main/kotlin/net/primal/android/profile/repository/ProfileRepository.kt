package net.primal.android.profile.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.events.repository.asProfileDataDO
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
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.report.ReportType
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.data.remote.api.explore.model.UsersResponse
import net.primal.data.remote.api.users.UserWellKnownApi
import net.primal.data.remote.api.users.UsersApi
import net.primal.domain.UserProfileSearchItem
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.publisher.PrimalPublisher

class ProfileRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val wellKnownApi: UserWellKnownApi,
    private val primalPublisher: PrimalPublisher,
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
                UserProfileSearchItem(metadata = it.asProfileDataDO(), followersCount = score)
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

    @Throws(NostrPublishException::class, SignException::class)
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

            primalPublisher.signPublishImportNostrEvent(
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
}
