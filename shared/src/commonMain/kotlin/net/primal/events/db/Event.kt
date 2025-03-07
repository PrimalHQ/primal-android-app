package net.primal.events.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val relay: String,
    val raw: String,
)
