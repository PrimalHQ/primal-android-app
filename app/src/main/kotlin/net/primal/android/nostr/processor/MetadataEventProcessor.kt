package net.primal.android.nostr.processor

import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.ContentMetadata
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.profile.db.ProfileMetadata
import net.primal.android.serialization.NostrJson

class MetadataEventProcessor(
    private val database: PrimalDatabase,
) : NostrEventProcessor {

    override val kind = NostrEventKind.Metadata

    override fun process(events: List<NostrEvent>) {
        database.profiles().upsertAll(
            events = events.map {
                it.asProfileMetadata()
            }
        )
    }

    private fun NostrEvent.asProfileMetadata(): ProfileMetadata {
        val metadata = NostrJson.decodeFromString<ContentMetadata>(this.content)
        return ProfileMetadata(
            eventId = this.id,
            ownerId = this.pubKey,
            createdAt = this.createdAt,
            raw = this.content,
            name = metadata.name,
            about = metadata.about,
            displayName = metadata.displayName,
            picture = metadata.picture,
            banner = metadata.banner,
            website = metadata.website,
        )
    }
}