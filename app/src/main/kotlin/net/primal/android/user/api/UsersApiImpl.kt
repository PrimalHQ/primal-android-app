package net.primal.android.user.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserRequestBody
import javax.inject.Inject

class UsersApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
) : UsersApi {

    override suspend fun getUserProfile(pubkey: String): UserProfileResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = "user_profile",
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = pubkey))
            )
        )

        return UserProfileResponse(
            metadata = queryResult.findNostrEvent(NostrEventKind.Metadata),
            profileStats = queryResult.findPrimalEvent(NostrEventKind.PrimalUserProfileStats),
        )
    }

    override suspend fun getUserContacts(pubkey: String): UserContactsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = "contact_list",
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = pubkey))
            )
        )

        return UserContactsResponse(
            contactsEvent = queryResult.findNostrEvent(NostrEventKind.Contacts),
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            eventResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventResources),
        )
    }
}
