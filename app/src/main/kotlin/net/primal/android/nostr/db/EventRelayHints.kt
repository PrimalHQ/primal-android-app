package net.primal.android.nostr.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventRelayHints(
    @PrimaryKey
    val eventId: String,
    val relays: List<String> = emptyList(),
)
