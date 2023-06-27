package net.primal.android.user.api

import kotlinx.serialization.encodeToString
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import net.primal.android.user.api.model.UserProfileRequestBody
import net.primal.android.user.api.model.UserProfileResponse
import javax.inject.Inject

class UsersApiImpl @Inject constructor(
    private val socketClient: SocketClient,
) : UsersApi {

    override suspend fun getUserProfile(pubkey: String): UserProfileResponse {
        val queryResult = socketClient.query(
            message = OutgoingMessage(
                primalVerb = "user_profile",
                optionsJson = NostrJson.encodeToString(UserProfileRequestBody(pubkey = pubkey))
            )
        )

        return UserProfileResponse(
            metadata = queryResult.nostrEvents.find {
                NostrEventKind.Metadata.value == it.kind
            },
            profileStats = queryResult.primalEvents.find {
                NostrEventKind.PrimalUserProfileStats.value == it.kind
            },
        )
    }
}
