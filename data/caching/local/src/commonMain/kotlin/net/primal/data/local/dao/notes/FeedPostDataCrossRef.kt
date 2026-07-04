package net.primal.data.local.dao.notes

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
