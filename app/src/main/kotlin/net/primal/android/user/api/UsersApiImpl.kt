package net.primal.android.user.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb.CONTACT_LIST
import net.primal.android.networking.primal.PrimalVerb.USER_PROFILE
import net.primal.android.networking.relays.RelayPool
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserRequestBody
import net.primal.android.user.domain.Relay
import javax.inject.Inject

class UsersApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
    private val relayPool: RelayPool,
    private val nostrNotary: NostrNotary,
) : UsersApi {

    override suspend fun getUserProfile(pubkey: String): UserProfileResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = USER_PROFILE,
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = pubkey))
            )
        )

        return UserProfileResponse(
            metadata = queryResult.findNostrEvent(NostrEventKind.Metadata),
            profileStats = queryResult.findPrimalEvent(NostrEventKind.PrimalUserProfileStats),
        )
    }

    override suspend fun getUserContacts(
        pubkey: String,
        extendedResponse: Boolean,
    ): UserContactsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = CONTACT_LIST,
                optionsJson = NostrJson.encodeToString(
                    UserRequestBody(pubkey = pubkey, extendedResponse = extendedResponse)
                )
            )
        )

        return UserContactsResponse(
            contactsEvent = queryResult.findNostrEvent(NostrEventKind.Contacts),
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            eventResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventResources),
        )
    }

    override suspend fun setUserContacts(
        ownerId: String,
        contacts: Set<String>,
        relays: List<Relay>
    ): NostrEvent {
        val signedNostrEvent = nostrNotary.signContactsNostrEvent(
            userId = ownerId,
            contacts = contacts,
            relays = relays,
        )
        relayPool.publishEvent(nostrEvent = signedNostrEvent)
        return signedNostrEvent
    }
}
