package net.primal.data.local.dao.events

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class EventRelayHints(
    @PrimaryKey
    val eventId: String,
    val relays: List<String> = emptyList(),
)
