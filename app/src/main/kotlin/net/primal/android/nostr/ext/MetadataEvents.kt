package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull


fun List<NostrEvent>.mapAsProfileMetadataPO() = map { it.asProfileMetadataPO() }

fun NostrEvent.asProfileMetadataPO(): ProfileMetadata {
    val metadata = NostrJson.decodeFromStringOrNull<ContentMetadata>(this.content)
    return ProfileMetadata(
        eventId = this.id,
        ownerId = this.pubKey,
        createdAt = this.createdAt,
        raw = NostrJson.encodeToString(this),
        handle = metadata?.name,
        internetIdentifier = metadata?.nip05,
        about = metadata?.about,
        displayName = metadata?.displayName,
        picture = metadata?.picture,
        banner = metadata?.banner,
        website = metadata?.website,
    )
}
