package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileData
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import net.primal.android.serialization.toJsonObject
import net.primal.android.wallet.api.decodeLNUrlOrNull
import net.primal.android.wallet.api.parseAsLNUrlOrNull


fun List<NostrEvent>.mapAsProfileDataPO() = map { it.asProfileDataPO() }

fun NostrEvent.asProfileDataPO(): ProfileData {
    val metadata = NostrJson.decodeFromStringOrNull<ContentMetadata>(this.content)

    return ProfileData(
        eventId = this.id,
        ownerId = this.pubKey,
        createdAt = this.createdAt,
        raw = NostrJson.encodeToString(this.toJsonObject()),
        handle = metadata?.name,
        internetIdentifier = metadata?.nip05,
        lightningAddress = metadata?.lud16,
        lnUrl = metadata?.lud16?.parseAsLNUrlOrNull()
            ?: metadata?.lud06?.decodeLNUrlOrNull(),
        about = metadata?.about,
        displayName = metadata?.displayName,
        picture = metadata?.picture,
        banner = metadata?.banner,
        website = metadata?.website,
    )
}
