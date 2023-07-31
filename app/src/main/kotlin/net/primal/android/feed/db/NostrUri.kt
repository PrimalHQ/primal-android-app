package net.primal.android.feed.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["eventId", "uri"]
)
/**
 * Represents a https://github.com/nostr-protocol/nips/blob/master/21.md URI.
 * Check also https://github.com/nostr-protocol/nips/blob/master/19.md for details on each encoded entity
 *
 */
data class NostrUri(
    val eventId: String,
    val uri: String,
    val profileId: String?,
    val noteId: String?,
    val name: String?,
)
