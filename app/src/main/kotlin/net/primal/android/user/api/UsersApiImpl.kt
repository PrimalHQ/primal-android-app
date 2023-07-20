package net.primal.android.user.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.filterNostrEvents
import net.primal.android.networking.sockets.filterPrimalEvents
import net.primal.android.networking.sockets.findNostrEvent
import net.primal.android.networking.sockets.findPrimalEvent
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserRequestBody
import javax.inject.Inject

class UsersApiImpl @Inject constructor(
    private val socketClient: SocketClient,
) : UsersApi {

    override suspend fun getUserProfile(pubkey: String): UserProfileResponse {
        val queryResult = socketClient.query(
            message = OutgoingMessage(
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
        val queryResult = socketClient.query(
            message = OutgoingMessage(
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
