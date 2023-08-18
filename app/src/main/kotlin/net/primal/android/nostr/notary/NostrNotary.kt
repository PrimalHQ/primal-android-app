package net.primal.android.nostr.notary

import fr.acinq.secp256k1.Hex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.crypto.toNpub
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asIdentifierTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.toNostrRelayMap
import net.primal.android.settings.api.model.AppSettingsDescription
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.Relay
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

    fun signShortTextNoteEvent(
        userId: String,
        tags: List<JsonArray>,
        noteContent: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ShortTextNote.value,
            tags = tags.toList(),
            content = noteContent,
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signAppSettingsSyncNostrEvent(
        userId: String
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
            content = NostrJson.encodeToString(
                AppSettingsDescription(description = "Sync app settings")
            ),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signAppSettingsNostrEvent(
        userId: String,
        appSettings: ContentAppSettings,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.ApplicationSpecificData.value,
            tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
            content = NostrJson.encodeToString(appSettings),
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signLikeReactionNostrEvent(
        userId: String,
        postId: String,
        postAuthorId: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Reaction.value,
            tags = listOf(postId.asEventIdTag(), postAuthorId.asPubkeyTag()),
            content = "+",
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signRepostNostrEvent(
        userId: String,
        postId: String,
        postAuthorId: String,
        postRawNostrEvent: String,
    ): NostrEvent {
        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Reposts.value,
            tags = listOf(postId.asEventIdTag(), postAuthorId.asPubkeyTag()),
            content = postRawNostrEvent,
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }

    fun signContactsNostrEvent(
        userId: String,
        contacts: Set<String>,
        relays: List<Relay>
    ): NostrEvent {
        val tags = contacts.map { it.asPubkeyTag() }
        val content = NostrJson.encodeToString(relays.toNostrRelayMap())

        return NostrUnsignedEvent(
            pubKey = userId,
            kind = NostrEventKind.Contacts.value,
            content = content,
            tags = tags
        ).signOrThrow(nsec = findNsecOrThrow(userId))
    }
}
