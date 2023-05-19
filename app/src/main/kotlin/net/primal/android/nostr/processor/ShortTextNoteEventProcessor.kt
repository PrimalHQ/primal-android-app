package net.primal.android.nostr.processor

import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.ShortTextNote
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind

class ShortTextNoteEventProcessor(
    private val database: PrimalDatabase
) : NostrEventProcessor {

    override val kind = NostrEventKind.ShortTextNote

    override fun process(events: List<NostrEvent>) {
        database.events().upsertAll(
            events = events.map {
                it.asShortTextNote()
            }
        )
    }

    private fun NostrEvent.asShortTextNote(): ShortTextNote = ShortTextNote(
        eventId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        sig = this.sig,
    )
}