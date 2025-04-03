package net.primal.data.repository.profile

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.asMapByKey
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.explore.model.UsersResponse
import net.primal.data.remote.api.users.UserWellKnownApi
import net.primal.data.remote.api.users.UsersApi
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.local.asProfileStatsDO
import net.primal.data.repository.mappers.remote.asProfileDataPO
import net.primal.data.repository.mappers.remote.asProfileStatsPO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserFollowersCountsOrNull
import net.primal.domain.UserProfileSearchItem
import net.primal.domain.model.ProfileData
import net.primal.domain.model.ProfileStats
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val usersApi: UsersApi,
    private val wellKnownApi: UserWellKnownApi,
    private val primalPublisher: PrimalPublisher,
) : ProfileRepository {

    override suspend fun fetchProfileId(primalName: String): String? =
        withContext(dispatcherProvider.io()) {
            wellKnownApi.fetchProfileId(primalName = primalName).names[primalName]
        }

    override suspend fun findProfileDataOrNull(profileId: String) =
        withContext(dispatcherProvider.io()) {
            database.profiles()
                .findProfileData(profileId = profileId)
                ?.asProfileDataDO()
        }

    override suspend fun findProfileStats(profileIds: List<String>): List<ProfileStats> =
        withContext(dispatcherProvider.io()) {
            database.profileStats().findProfileStats(profileIds = profileIds)
                .map { it.asProfileStatsDO() }
        }

    override suspend fun findProfileData(profileIds: List<String>) =
        withContext(dispatcherProvider.io()) {
            database.profiles().findProfileData(profileIds = profileIds)
                .map { it.asProfileDataDO() }
        }

    override fun observeProfileData(profileId: String) =
        database.profiles().observeProfileData(profileId = profileId)
            .filterNotNull()
            .map { it.asProfileDataDO() }

    override fun observeProfileData(profileIds: List<String>) =
        database.profiles().observeProfilesData(profileIds = profileIds)
            .map { it.map { it.asProfileDataDO() } }

    override fun observeProfileStats(profileId: String) =
        database.profileStats().observeProfileStats(profileId = profileId)
            .filterNotNull()
            .map { it.asProfileStatsDO() }

    override suspend fun fetchUserProfileFollowedBy(
        profileId: String,
        userId: String,
        limit: Int,
    ): List<ProfileData> =
        withContext(dispatcherProvider.io()) {
            val users = usersApi.getUserProfileFollowedBy(profileId, userId, limit)

            val primalUserNames = users.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = users.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = users.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = users.cdnResources.flatMapNotNullAsCdnResource()
            val blossomServers = users.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profiles = users.metadataEvents.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
            database.profiles().insertOrUpdateAll(data = profiles)
            profiles.map { it.asProfileDataDO() }
        }

    override suspend fun fetchProfile(profileId: String) =
        withContext(dispatcherProvider.io()) {
            val response = retryNetworkCall { usersApi.getUserProfile(userId = profileId) }
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
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

            profileMetadata?.asProfileDataDO()
        }

    override suspend fun fetchProfiles(profileIds: List<String>): List<ProfileData> =
        withContext(dispatcherProvider.io()) {
            val response = retryNetworkCall { usersApi.getUserProfilesMetadata(userIds = profileIds.toSet()) }

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profiles = response.metadataEvents.map {
                it.asProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNames,
                    primalPremiumInfo = primalPremiumInfo,
                    primalLegendProfiles = primalLegendProfiles,
                    blossomServers = blossomServers,
                )
            }

            database.profiles().insertOrUpdateAll(data = profiles)

            profiles.map { it.asProfileDataDO() }
        }

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> =
        withContext(dispatcherProvider.io()) {
            val response = apiBlock()
            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
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

    override suspend fun fetchFollowers(userId: String) =
        queryRemoteUsers {
            usersApi.getUserFollowers(userId = userId)
        }

    override suspend fun fetchFollowing(userId: String) =
        queryRemoteUsers {
            usersApi.getUserFollowing(userId = userId)
        }

    override suspend fun reportAbuse(
        userId: String,
        reportType: ReportType,
        profileId: String,
        eventId: String?,
        articleId: String?,
    ) {
        withContext(dispatcherProvider.io()) {
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

    override suspend fun isUserFollowing(userId: String, targetUserId: String) =
        withContext(dispatcherProvider.io()) {
            usersApi.isUserFollowing(userId, targetUserId)
        }
}
