package net.primal.android.user.api

import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.user.api.model.BookmarksResponse
import net.primal.android.user.api.model.FollowListRequestBody
import net.primal.android.user.api.model.IsUserFollowingRequestBody
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileFollowedByRequestBody
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserProfilesRequestBody
import net.primal.android.user.api.model.UserProfilesResponse
import net.primal.android.user.api.model.UserRequestBody
import net.primal.android.user.api.model.UsersRelaysResponse
import net.primal.android.user.api.model.UsersRequestBody
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.errors.WssException
import net.primal.data.remote.PrimalVerb
import net.primal.domain.nostr.NostrEventKind

class UsersApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : UsersApi {

    override suspend fun getUserProfile(userId: String): UserProfileResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.USER_PROFILE.id,
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = userId)),
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
                primalVerb = PrimalVerb.USER_PROFILE_FOLLOWED_BY.id,
                optionsJson = NostrJson.encodeToString(
                    UserProfileFollowedByRequestBody(
                        profileId = profileId,
                        userId = userId,
                        limit = limit,
                    ),
                ),
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
                primalVerb = PrimalVerb.FOLLOW_LIST.id,
                optionsJson = NostrJson.encodeToString(
                    FollowListRequestBody(pubkey = userId, extendedResponse = false),
                ),
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
                primalVerb = PrimalVerb.USER_INFOS.id,
                optionsJson = NostrJson.encodeToString(
                    UserProfilesRequestBody(userIds = userIds),
                ),
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
                primalVerb = PrimalVerb.FOLLOW_LIST.id,
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = userId)),
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
                primalVerb = PrimalVerb.USER_FOLLOWERS.id,
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = userId)),
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
                primalVerb = PrimalVerb.USER_RELAYS_2.id,
                optionsJson = NostrJson.encodeToString(UsersRequestBody(pubkeys = userIds)),
            ),
        )

        return UsersRelaysResponse(
            cachedRelayListEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserRelaysList),
        )
    }

    override suspend fun getDefaultRelays(): List<String> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = PrimalVerb.DEFAULT_RELAYS.id),
        )

        val list = queryResult.findPrimalEvent(NostrEventKind.PrimalDefaultRelaysList)
        val content = list?.content
        if (content.isNullOrEmpty()) throw WssException("Invalid content.")

        return NostrJson.decodeFromString<List<String>>(list.content)
    }

    override suspend fun isUserFollowing(userId: String, targetUserId: String): Boolean {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.IS_USER_FOLLOWING.id,
                optionsJson = NostrJson.encodeToString(
                    IsUserFollowingRequestBody(userId = userId, targetUserId = targetUserId),
                ),
            ),
        )

        val primalEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalIsUserFollowing)
            ?: throw WssException("No response")
        return primalEvent.content.toBooleanStrictOrNull()
            ?: throw WssException("Invalid response.")
    }

    override suspend fun getUserBookmarksList(userId: String): BookmarksResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_BOOKMARKS_LIST.id,
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = userId)),
            ),
        )
        return BookmarksResponse(
            bookmarksListEvent = queryResult.findNostrEvent(NostrEventKind.BookmarksList),
        )
    }
}
