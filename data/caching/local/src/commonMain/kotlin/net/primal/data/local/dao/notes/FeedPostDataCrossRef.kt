package net.primal.data.local.dao.notes

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["ownerId", "feedSpec", "eventId"], unique = true),
        Index(value = ["ownerId", "feedSpec", "position"]),
    ],
)
data class FeedPostDataCrossRef(
    @PrimaryKey(autoGenerate = true)
    val position: Long = 0L,
    val ownerId: String,
    val feedSpec: String,
    val eventId: String,
)
