package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileData
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull


fun List<NostrEvent>.mapAsProfileDataPO() = map { it.asProfileDataPO() }

fun NostrEvent.asProfileDataPO(): ProfileData {
    val metadata = NostrJson.decodeFromStringOrNull<ContentMetadata>(this.content)

    return ProfileData(
        eventId = this.id,
        ownerId = this.pubKey,
        createdAt = this.createdAt,
        raw = NostrJson.encodeToString(this),
        handle = metadata?.name,
        internetIdentifier = metadata?.nip05,
        lightningAddress = metadata?.lud16,
        about = metadata?.about,
        displayName = metadata?.displayName,
        picture = metadata?.picture,
        banner = metadata?.banner,
        website = metadata?.website,
    )
}
