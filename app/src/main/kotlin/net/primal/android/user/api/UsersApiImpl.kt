package net.primal.android.user.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.Verb.*
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserRequestBody
import net.primal.android.user.services.ContactsService
import javax.inject.Inject

class UsersApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
    private val relayPool: RelayPool,
    private val nostrNotary: NostrNotary,
    private val contactsService: ContactsService
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

    override suspend fun getUserContacts(pubkey: String): UserContactsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = CONTACT_LIST,
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

    override suspend fun follow(ownerPubkey: String, followedPubkey: String, relays: List<String>): Set<String>? {
        val following = contactsService.prepareContacts().toMutableSet()

        following.add(followedPubkey)

        try {
            relayPool.publishEvent(
                nostrEvent = nostrNotary.signContactsNostrEvent(
                    userId = ownerPubkey,
                    contacts = following,
                    relays = relays
                )
            )
        } catch (ex: NostrPublishException) {
            following.remove(followedPubkey)
            throw ex
        }

        return following
    }

    override suspend fun unfollow(ownerPubkey: String, unfollowedPubkey: String, relays: List<String>): Set<String>? {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = CONTACT_LIST,
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = ownerPubkey, extendedResponse = false))
            )
        )

        val contacts = queryResult.findNostrEvent(NostrEventKind.Contacts)
        val following = contacts?.tags?.parseFollowings()?.toMutableSet() ?: return null

        following.remove(unfollowedPubkey)

        try {
            relayPool.publishEvent(
                nostrEvent = nostrNotary.signContactsNostrEvent(
                    userId = ownerPubkey,
                    contacts = following,
                    relays = relays
                )
            )
        } catch (ex: NostrPublishException) {
            following.add(unfollowedPubkey)
            throw ex
        }

        return following
    }
}
