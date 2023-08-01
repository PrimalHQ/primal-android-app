package net.primal.android.feed.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["eventId", "uri"]
)
data class NostrUri(
    val eventId: String,
    val uri: String,
    val profileId: String? = null,
    val noteId: String? = null,
    val name: String? = null,
)
