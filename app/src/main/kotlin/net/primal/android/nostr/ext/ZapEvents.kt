package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.feed.db.NoteZapData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapAsNoteZapDO() = mapNotNull { it.extractZapRequestOrNull()?.asNoteZap() }

private fun NostrEvent.extractZapRequestOrNull() =
    NostrJson.decodeFromStringOrNull<NostrEvent>(tags.findFirstZapRequest())

private fun NostrEvent.asNoteZap(): NoteZapData? {
    val receiverId = this.tags.findFirstProfileId()
    val noteId = this.tags.findFirstEventId()
    val amount = this.tags.findFirstZapAmount()
    if (receiverId == null || noteId == null || amount == null) return null

    return NoteZapData(
        zapSenderId = this.pubKey,
        zapReceiverId = receiverId,
        noteId = noteId,
        zappedAt = this.createdAt,
        amountInMillisats = amount,
        message = this.content,
    )
}
