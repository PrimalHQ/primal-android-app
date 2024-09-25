package net.primal.android.nostr.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventHints(
    @PrimaryKey
    val eventTag: String,
    val eventId: String? = null,
    val relays: List<String> = emptyList(),
    val isBookmarked: Boolean? = null,
)
