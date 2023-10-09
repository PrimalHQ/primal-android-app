package net.primal.android.user.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb.CONTACT_LIST
import net.primal.android.networking.primal.PrimalVerb.USER_INFOS
import net.primal.android.networking.primal.PrimalVerb.USER_PROFILE
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.user.api.model.ContactsRequestBody
import net.primal.android.user.api.model.GetUserProfilesRequest
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserRequestBody
import net.primal.android.user.domain.Relay
import timber.log.Timber
import javax.inject.Inject

class UsersApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val relaysManager: RelaysManager,
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
                    ContactsRequestBody(pubkey = pubkey, extendedResponse = extendedResponse)
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
        relaysManager.publishEvent(nostrEvent = signedNostrEvent)
        return signedNostrEvent
    }

    override suspend fun setUserProfile(
        ownerId: String,
        contentMetadata: ContentMetadata
    ): NostrEvent {
        val signedNostrEvent = nostrNotary.signMetadataNostrEvent(
            userId = ownerId,
            metadata = contentMetadata,
        )
        relaysManager.publishEvent(nostrEvent = signedNostrEvent)
        return signedNostrEvent
    }

    override suspend fun getUserProfiles(pubkeys: Set<String>): List<NostrEvent> {
        return try {
            val queryResult = primalApiClient.query(
                message = PrimalCacheFilter(
                    primalVerb = USER_INFOS,
                    optionsJson = NostrJson.encodeToString(
                        GetUserProfilesRequest(pubkeys = pubkeys)
                    )
                )
            )

            queryResult.filterNostrEvents(NostrEventKind.Metadata)
        } catch (error: WssException) {
            Timber.e(error)
            emptyList()
        }
    }
}
