package net.primal.data.remote.api.users

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.explore.model.UsersResponse
import net.primal.data.remote.api.users.model.BookmarksResponse
import net.primal.data.remote.api.users.model.FollowListRequestBody
import net.primal.data.remote.api.users.model.IsUserFollowingRequestBody
import net.primal.data.remote.api.users.model.UserContactsResponse
import net.primal.data.remote.api.users.model.UserProfileFollowedByRequestBody
import net.primal.data.remote.api.users.model.UserProfileResponse
import net.primal.data.remote.api.users.model.UserProfilesRequestBody
import net.primal.data.remote.api.users.model.UserProfilesResponse
import net.primal.data.remote.api.users.model.UserRequestBody
import net.primal.data.remote.api.users.model.UsersRelaysResponse
import net.primal.data.remote.api.users.model.UsersRequestBody
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind

internal class UsersApiImpl(
    private val primalApiClient: PrimalApiClient,
) : UsersApi {

    override suspend fun getUserProfile(userId: String): UserProfileResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.USER_PROFILE.id,
                optionsJson = UserRequestBody(pubkey = userId).encodeToJsonString(),
            ),
        )

        return UserProfileResponse(
            metadata = queryResult.findNostrEvent(NostrEventKind.Metadata),
            profileStats = queryResult.findPrimalEvent(NostrEventKind.PrimalUserProfileStats),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getUserProfileFollowedBy(
        profileId: String,
        userId: String,
        limit: Int,
    ): UserProfilesResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.USER_PROFILE_FOLLOWED_BY.id,
                optionsJson = UserProfileFollowedByRequestBody(
                    profileId = profileId,
                    userId = userId,
                    limit = limit,
                ).encodeToJsonString(),
            ),
        )

        return UserProfilesResponse(
            metadataEvents = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
        )
    }

    override suspend fun getUserFollowList(userId: String): UserContactsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.CONTACT_LIST.id,
                optionsJson = FollowListRequestBody(
                    pubkey = userId,
                    extendedResponse = false,
                ).encodeToJsonString(),
            ),
        )

        return UserContactsResponse(
            followListEvent = queryResult.findNostrEvent(NostrEventKind.FollowList),
            followMetadataList = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
        )
    }

    override suspend fun getUserProfilesMetadata(userIds: Set<String>): UserProfilesResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.USER_INFOS.id,
                optionsJson = UserProfilesRequestBody(userIds = userIds).encodeToJsonString(),
            ),
        )

        return UserProfilesResponse(
            metadataEvents = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
        )
    }

    override suspend fun getUserFollowing(userId: String): UsersResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.CONTACT_LIST.id,
                optionsJson = UserRequestBody(pubkey = userId).encodeToJsonString(),
            ),
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            followerCounts = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getUserFollowers(userId: String): UsersResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.USER_FOLLOWERS.id,
                optionsJson = UserRequestBody(pubkey = userId).encodeToJsonString(),
            ),
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            followerCounts = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getUserRelays(userIds: List<String>): UsersRelaysResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.USER_RELAYS_2.id,
                optionsJson = UsersRequestBody(pubkeys = userIds).encodeToJsonString(),
            ),
        )

        return UsersRelaysResponse(
            cachedRelayListEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserRelaysList),
        )
    }

    override suspend fun getDefaultRelays(): List<String> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = net.primal.data.remote.PrimalVerb.DEFAULT_RELAYS.id),
        )

        val list = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultRelaysList)
        val content = list?.content
        if (content.isNullOrEmpty()) throw NetworkException("Invalid content.")

        return list.content.decodeFromJsonStringOrNull<List<String>>() ?: emptyList()
    }

    override suspend fun isUserFollowing(userId: String, targetUserId: String): Boolean {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.IS_USER_FOLLOWING.id,
                optionsJson = IsUserFollowingRequestBody(
                    userId = userId,
                    targetUserId = targetUserId,
                ).encodeToJsonString(),
            ),
        )

        val primalEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalIsUserFollowing)
            ?: throw NetworkException("No response")
        return primalEvent.content.toBooleanStrictOrNull()
            ?: throw NetworkException("Invalid response.")
    }

    override suspend fun getUserBookmarksList(userId: String): BookmarksResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.GET_BOOKMARKS_LIST.id,
                optionsJson = UserRequestBody(pubkey = userId).encodeToJsonString(),
            ),
        )
        return BookmarksResponse(
            bookmarksListEvent = queryResult.findNostrEvent(NostrEventKind.BookmarksList),
        )
    }

    override suspend fun getRecommendedBlossomServers(): List<String> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = net.primal.data.remote.PrimalVerb.RECOMMENDED_BLOSSOM_SERVERS.id),
        )

        val list = queryResult.findPrimalEvent(NostrEventKind.PrimalRecommendedBlossomServer)
        val content = list?.content
        if (content.isNullOrEmpty()) throw NetworkException("Invalid content.")

        return list.content.decodeFromJsonStringOrNull<List<String>>() ?: emptyList()
    }
}
