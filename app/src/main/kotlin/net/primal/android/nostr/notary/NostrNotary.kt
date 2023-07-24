package net.primal.android.nostr.notary

import fr.acinq.secp256k1.Hex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.crypto.toNpub
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import net.primal.android.settings.api.model.AppSettingsDescription
import net.primal.android.user.credentials.CredentialsStore
import java.time.Instant
import javax.inject.Inject


class NostrNotary @Inject constructor(
    private val credentialsStore: CredentialsStore,
) {

    private fun findNsecOrThrow(pubkey: String): String {
        return try {
            val npub = Hex.decode(pubkey).toNpub()
            credentialsStore.findOrThrow(npub = npub).nsec
        } catch (error: IllegalArgumentException) {
            throw NostrSignUnauthorized()
        }
    }

    fun signAppSettingsSyncNostrEvent(
        userId: String
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            createdAt = Instant.now().epochSecond,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf(
                buildJsonArray {
                    add("d")
                    add("${UserAgentProvider.APP_NAME} App")
                }
            ),
            content = NostrJson.encodeToString(
                AppSettingsDescription(description = "Sync app settings")
            ),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signLikeReactionNostrEvent(
        userId: String,
        postId: String,
        postPubkey: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            createdAt = Instant.now().epochSecond,
            kind = NostrEventKind.Reaction.value,
            tags = listOf(
                buildJsonArray {
                    add("e")
                    add(postId)
                },
                buildJsonArray {
                    add("p")
                    add(postPubkey)
                },
            ),
            content = "+",
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

}
