package net.primal.android.nostr.ext

import kotlinx.serialization.decodeFromString
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.crypto.toNpub
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.serialization.NostrJson
import org.spongycastle.util.encoders.Hex


fun List<NostrEvent>.mapAsProfileMetadata() = map { it.asProfileMetadata() }

fun NostrEvent.asProfileMetadata(): ProfileMetadata {
    val metadata = NostrJson.decodeFromString<ContentMetadata>(this.content)
    return ProfileMetadata(
        eventId = this.id,
        ownerId = this.pubKey,
        createdAt = this.createdAt,
        raw = this.content,
        name = metadata.name,
        internetIdentifier = metadata.nip05,
        about = metadata.about,
        displayName = metadata.displayName,
        picture = metadata.picture,
        banner = metadata.banner,
        website = metadata.website,
    )
}

fun ProfileMetadata.displayNameUiFriendly(): String {
    return when {
        displayName?.isNotEmpty() == true -> displayName
        name?.isNotEmpty() == true -> name
        else -> ownerId.asEllipsizedNpub()
    }
}

fun String.asEllipsizedNpub(): String = Hex.decode(this).toNpub().ellipsizeMiddle(size = 8)